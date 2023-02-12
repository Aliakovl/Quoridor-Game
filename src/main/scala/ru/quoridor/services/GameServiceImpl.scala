package ru.quoridor.services

import ru.quoridor.model.GameException.{
  GameHasFinishedException,
  GameInterloperException,
  WrongPlayersTurnException
}
import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model.game.{Game, Move, PawnMove, Player}
import ru.quoridor.model.game.geometry.Board
import ru.quoridor.storage.GameStorage
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.Implicits.TaggedOps
import zio.{Task, ZIO}

import java.util.UUID

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
      id = UUID.randomUUID().tag[Game]
      newGame <- gameStorage.insert(id, gameId, newState, winner)
    } yield newGame
  }

  override def usersHistory(userId: ID[User]): Task[List[GamePreView]] = {
    for {
      gameIds <- gameStorage.history(userId)
      gamePreViews <- ZIO.foreachPar(gameIds)(gameStorage.findParticipants)
    } yield gamePreViews
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
      history <- ZIO.foreachPar(gameIds)(gameStorage.find)
    } yield history
  }
}
