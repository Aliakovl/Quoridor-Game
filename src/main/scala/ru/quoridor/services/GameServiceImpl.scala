package ru.quoridor.services

import ru.quoridor.model.GameException.{
  GameHasFinishedException,
  GameInterloperException,
  WrongPlayersTurnException
}
import ru.quoridor.model.User
import ru.quoridor.model.game.{Game, Move, PawnMove, Player}
import ru.quoridor.model.game.geometry.Board
import ru.quoridor.storage.GameStorage
import zio.{Task, ZIO}

import java.util.UUID

class GameServiceImpl(gameStorage: GameStorage) extends GameService {

  override def findGame(gameId: UUID): Task[Game] = {
    gameStorage.find(gameId)
  } // TODO: вернуть проверку на принадлежность игрока игре

  override def makeMove(
      gameId: UUID,
      userId: UUID,
      move: Move
  ): Task[Game] = {
    for {
      game <- gameStorage.find(gameId)

      either = for {
        _ <- Either.cond(
          game.state.players.toList.exists(_.userId == userId),
          (),
          GameInterloperException(userId, gameId)
        )
        _ <- Either.cond(
          game.state.players.activePlayer.userId == userId,
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
          case Player(id, login, _, _, target)
              if Board.isPawnOnEdge(pawnPosition, target) =>
            User(id, login)
        }
      }.flatten
      newGame <- gameStorage.insert(gameId, newState, winner)
    } yield newGame
  }

  override def gameHistory(
      gameId: UUID,
      userId: UUID
  ): Task[List[Game]] = {
    for {
      game <- gameStorage.find(gameId)
      _ <- ZIO.cond(
        game.state.players.toList.exists(_.userId == userId),
        (),
        GameInterloperException(userId, gameId)
      )

      gameIds <- gameStorage.gameHistory(gameId)
      history <-
        if (gameIds.isEmpty) { ZIO.succeed(List.empty[Game]) }
        else {
          ZIO.foreachPar(gameIds)(gameStorage.find)
        }
    } yield history
  }
}

object GameServiceImpl {
  def apply(gameStorage: GameStorage): GameServiceImpl =
    new GameServiceImpl(gameStorage)
}
