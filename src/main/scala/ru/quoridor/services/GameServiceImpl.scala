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
import ru.utils.tagging.ID
import zio.{Task, ZIO}

class GameServiceImpl(gameStorage: GameStorage) extends GameService {

  override def findGame(gameId: ID[Game]): Task[Game] = {
    gameStorage.find(gameId)
  } // TODO: вернуть проверку на принадлежность игрока игре

  override def makeMove(
      gameId: ID[Game],
      userId: ID[User],
      move: Move
  ): Task[Game] = {
    for {
      game <- gameStorage.find(gameId)

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
          case Player(id, login, _, _, target)
              if Board.isPawnOnEdge(pawnPosition, target) =>
            User(id, login)
        }
      }.flatten
      newGame <- gameStorage.insert(gameId, newState, winner)
    } yield newGame
  }

  override def gameHistory(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[List[Game]] = {
    for {
      game <- gameStorage.find(gameId)
      _ <- ZIO.cond(
        game.state.players.toList.exists(_.id == userId),
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
