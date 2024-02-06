package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.GameException.*
import dev.aliakovl.quoridor.model.*
import dev.aliakovl.quoridor.dao.{GameDao, UserDao}
import dev.aliakovl.quoridor.engine.game.Move
import dev.aliakovl.quoridor.engine.game.geometry.{PawnPosition, WallPosition}
import dev.aliakovl.quoridor.pubsub.GamePubSub
import dev.aliakovl.utils.ZIOExtensions.*
import dev.aliakovl.utils.tagging.ID
import zio.stream.ZStream
import zio.*

class GameServiceLive(
    gameDao: GameDao,
    gamePubSub: GamePubSub,
    userDao: UserDao
) extends GameService:
  override def findGame(gameId: ID[Game]): Task[GameResponse] = {
    for {
      game <- gameDao.find(gameId)
      users <- userDao.findUsers(game.state.players.toList.map(_.id))
    } yield GameResponse.fromGame(users)(game)
  }

  override def makeMove(
      gameId: ID[Game],
      userId: ID[User],
      move: Move
  ): Task[GameResponse] = {
    for {
      game <- gameDao.find(gameId)
      // TODO: move to a higher level
      _ <- ZIO.when(game.state.players.toList.exists(_.id == userId))(
        ZIO.fail(GameInterloperException(userId, gameId))
      )
      // TODO: move to engine
      _ <- ZIO.when(game.state.players.activePlayer.id == userId)(
        ZIO.fail(WrongPlayersTurnException(gameId))
      )
      // TODO: move to engine
      _ <- ZIO.when(game.winner.isEmpty)(
        ZIO.fail(GameHasFinishedException(gameId))
      )
      newState <- ZIO.fromEither(move.makeAt(game.state))
      winnerId = move.getWinner(game.state)
      winner <- ZIO
        .fromOption(winnerId)
        .flatMap(id => userDao.findById(id).catchAll(_ => ZIO.fail(None)))
        .unsome
      _ <- gameDao.insert(gameId, game.step + 1, newState, move, winner)
      users <- userDao.findUsers(newState.players.toList.map(_.id))
      newGame = GameResponse(
        gameId,
        step = game.step + 1,
        state = StateResponse.fromState(users)(newState),
        winner = winner
      )
      _ <- gamePubSub.publish(newGame)
    } yield newGame
  }

  override def usersHistory(userId: ID[User]): Task[List[GamePreView]] = {
    for {
      gameIds <- gameDao.history(userId)
      gamePreViews <- ZIO.foreachPar(gameIds)(gameDao.findParticipants)
    } yield gamePreViews.filter(_.players.length > 1)
  }

  override def gameHistory(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[List[GameResponse]] = {
    for {
      game <- gameDao.find(gameId)
      // TODO: move to a higher level
      _ <- ZIO.unless(game.state.players.toList.exists(_.id == userId))(
        ZIO.fail(GameInterloperException(userId, gameId))
      )
      users <- userDao.findUsers(game.state.players.toList.map(_.id))
      lastStep <- gameDao.lastStep(gameId)
      history <- ZIO
        .foreachPar((0 to lastStep).toList)(
          gameDao.find(gameId, _)
        )
        .map(_.map(GameResponse.fromGame(users)))
    } yield history
  }

  override def possiblePawnMoves(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[List[PawnPosition]] = {
    for {
      game <- gameDao.find(gameId)
      // TODO: move to a higher level
      _ <- ZIO
        .succeed(game.state.players.toList.exists(_.id == userId))
        .orFail(GameInterloperException(userId, gameId))
      _ <- ZIO
        .succeed(game.winner.isEmpty)
        .orFail(GameHasFinishedException(gameId))
    } yield game.state.possibleSteps
  }

  def possibleWallMoves(
      gameId: ID[Game]
  ): Task[Set[WallPosition]] = {
    for {
      game <- gameDao.find(gameId)
      _ <- ZIO
        .succeed(game.winner.isEmpty)
        .orFail(GameHasFinishedException(gameId))
    } yield game.state.availableWalls
  }

  override def subscribeOnGame(
      gameId: ID[Game]
  ): Task[ZStream[Any, Throwable, GameResponse]] = ZIO.succeed(
    ZStream.fromZIO(findGame(gameId)) ++ ZStream
      .unwrapScoped(gamePubSub.subscribe(gameId))
      .takeWhile(_.winner.isEmpty) ++ ZStream.fromZIO(findGame(gameId))
  )

object GameServiceLive:
  val live: URLayer[GameDao & GamePubSub & UserDao, GameService] =
    ZLayer.fromFunction(new GameServiceLive(_, _, _))
