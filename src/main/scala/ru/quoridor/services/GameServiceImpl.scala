package ru.quoridor.services

import ru.quoridor.model.GameException.{
  GameHasFinishedException,
  GameInterloperException,
  WrongPlayersTurnException
}
import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model.game.{Game, Move, PawnMove, Player}
import ru.quoridor.model.game.geometry.{Board, PawnPosition, WallPosition}
import ru.quoridor.dao.GameDao
import ru.utils.ZIOExtensions.OrFail
import ru.utils.tagging.ID
import zio.{Task, ZIO}

class GameServiceImpl(gameDao: GameDao) extends GameService {

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
      winner = Some(move).collect { case PawnMove(pawnPosition) =>
        Some(game.state.players.activePlayer).collect {
          case Player(id, username, _, _, target)
              if Board.isPawnOnEdge(pawnPosition, target) =>
            User(id, username)
        }
      }.flatten
      _ <- gameDao.insert(gameId, game.step + 1, newState, move, winner)
    } yield Game(
      gameId,
      step = game.step + 1,
      state = newState,
      winner = winner
    )
  }

  override def usersHistory(userId: ID[User]): Task[List[GamePreView]] = {
    for {
      gameIds <- gameDao.history(userId)
      gamePreViews <- ZIO.foreachPar(gameIds)(gameDao.findParticipants)
    } yield gamePreViews
  }

  override def gameHistory(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[List[Game]] = {
    for {
      game <- gameDao.find(gameId)
      _ <- ZIO.cond(
        game.state.players.toList.exists(_.id == userId),
        (),
        GameInterloperException(userId, gameId)
      )

      lastStep <- gameDao.lastStep(gameId)
      history <- ZIO.foreachPar((0 to lastStep).toList)(
        gameDao.find(gameId, _)
      )
    } yield history
  }

  override def availablePawnMoves(
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

  def availableWallMoves(
      gameId: ID[Game]
  ): Task[Set[WallPosition]] = {
    for {
      game <- gameDao.find(gameId)
      _ <- ZIO
        .succeed(game.winner.isEmpty)
        .orFail(GameHasFinishedException(gameId))
    } yield Board.availableWalls(
      game.state.walls,
      game.state.players.toList.map {
        case Player(_, _, pawnPosition, _, target) =>
          (pawnPosition, target)
      }
    )
  }
}
