package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.GameException.{
  GameHasFinishedException,
  GameInterloperException,
  WrongPlayersTurnException
}
import dev.aliakovl.quoridor.model.{GamePreView, User}
import dev.aliakovl.quoridor.model.game.{Game, Move, Player}
import dev.aliakovl.quoridor.model.game.geometry.{
  Board,
  PawnPosition,
  WallPosition
}
import dev.aliakovl.quoridor.dao.GameDao
import dev.aliakovl.quoridor.pubsub.GamePubSub
import dev.aliakovl.utils.ZIOExtensions.*
import dev.aliakovl.utils.tagging.ID
import zio.stream.ZStream
import zio.*

class GameServiceLive(
    gameDao: GameDao,
    gamePubSub: GamePubSub
) extends GameService:
  override def findGame(gameId: ID[Game]): Task[Game] = {
    gameDao.find(gameId)
  } // TODO: вернуть проверку на принадлежность игрока игре

  override def makeMove(
      gameId: ID[Game],
      userId: ID[User],
      move: Move
  ): Task[Game] = {
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
      winner = Some(move).collect { case Move.PawnMove(pawnPosition) =>
        Some(game.state.players.activePlayer).collect {
          case Player(id, username, _, _, target)
              if Board.isPawnOnEdge(pawnPosition, target) =>
            User(id, username)
        }
      }.flatten
      _ <- gameDao.insert(gameId, game.step + 1, newState, move, winner)
      newGame = Game(
        gameId,
        step = game.step + 1,
        state = newState,
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
            case Player(_, _, pawnPosition, _, target) =>
              (pawnPosition, target)
          }
        )
      else Set.empty[WallPosition]
    }
  }

  override def subscribeOnGame(
      gameId: ID[Game]
  ): Task[ZStream[Any, Throwable, Game]] = ZIO.succeed(
    ZStream.fromZIO(findGame(gameId)) ++ ZStream
      .unwrapScoped(gamePubSub.subscribe(gameId))
      .takeWhile(_.winner.isEmpty) ++ ZStream.fromZIO(findGame(gameId))
  )

object GameServiceLive:
  val live: URLayer[GameDao & GamePubSub, GameService] =
    ZLayer.fromFunction(new GameServiceLive(_, _))
