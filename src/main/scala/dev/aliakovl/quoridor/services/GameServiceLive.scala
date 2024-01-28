package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.GameException.{
  GameHasFinishedException,
  GameInterloperException,
  WrongPlayersTurnException
}
import dev.aliakovl.quoridor.model.{Game, GamePreView, StateResponse, User}
import dev.aliakovl.quoridor.dao.{GameDao, UserDao}
import dev.aliakovl.quoridor.engine.game.{Move, Player}
import dev.aliakovl.quoridor.engine.game.geometry.{
  Board,
  PawnPosition,
  WallPosition
}
import dev.aliakovl.quoridor.pubsub.GamePubSub
import dev.aliakovl.quoridor.services.model.GameResponse
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
    } yield GameResponse.fromGame(game, users)
  }

  override def makeMove(
      gameId: ID[Game],
      userId: ID[User],
      move: Move
  ): Task[GameResponse] = {
    for {
      game <- gameDao.find(gameId)
      either = for {
        _ <- Either.cond(
          game.state.players.toList.exists(_.id == userId),
          (),
          GameInterloperException(userId, gameId)
        )
        _ <- Either.cond(
          game.state.players.activePlayer.id == userId,
          (),
          WrongPlayersTurnException(gameId)
        )
        _ <- Either.cond(
          game.winner.isEmpty,
          (),
          GameHasFinishedException(gameId)
        )
        newState <- move.makeAt(game.state)
      } yield newState

      newState <- ZIO.fromEither(either)
      winnerId = Some(move).collect { case Move.PawnMove(pawnPosition) =>
        Some(game.state.players.activePlayer).collect {
          case Player(id, _, _, target)
              if Board.isPawnOnEdge(pawnPosition, target) =>
            id
        }
      }.flatten
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
  ): Task[List[Game]] = {
    for {
      game <- gameDao.find(gameId)
      _ <- ZIO.unless(game.state.players.toList.exists(_.id == userId))(
        ZIO.fail(GameInterloperException(userId, gameId))
      )
      lastStep <- gameDao.lastStep(gameId)
      history <- ZIO.foreachPar((0 to lastStep).toList)(
        gameDao.find(gameId, _)
      )
    } yield history
  }

  override def possiblePawnMoves(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[List[PawnPosition]] = {
    for {
      game <- gameDao.find(gameId)
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
    } yield {
      if game.state.players.activePlayer.wallsAmount > 0 then
        Board.availableWalls(
          game.state.walls,
          game.state.players.toList.map {
            case Player(_, pawnPosition, _, target) =>
              (pawnPosition, target)
          }
        )
      else Set.empty[WallPosition]
    }
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
