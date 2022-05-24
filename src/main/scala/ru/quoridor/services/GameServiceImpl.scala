package ru.quoridor.services

import cats.effect.Async
import cats.implicits._
import ru.quoridor.GameException.{GameHasFinishedException, GameInterloperException, WrongPlayersTurnException}
import ru.quoridor.User
import ru.quoridor.game.geometry.Board
import ru.quoridor.game.{Game, Move, PawnMove, Player}
import ru.quoridor.storage.{GameStorage, ProtoGameStorage, UserStorage}
import ru.utils.Typed.ID


class GameServiceImpl[F[_]](gameStorage: GameStorage[F])
                           (implicit F: Async[F]) extends GameService[F] {

  override def findGame(gameId: ID[Game], userId: ID[User]): F[Game] = {
    for {
      game <- gameStorage.find(gameId)

      _ <- F.fromEither{
        Either.cond(game.state.players.toList.exists(_.id == userId), (), GameInterloperException(userId, gameId))
      }

    } yield game
  }

  override def makeMove(gameId: ID[Game], userId: ID[User], move: Move): F[Game] = {
    for {
      game <- gameStorage.find(gameId)

      either = for {
        _ <- Either.cond(game.state.players.toList.exists(_.id == userId), (), GameInterloperException(userId, gameId))
        _ <- Either.cond(game.state.players.activePlayer.id == userId, (), WrongPlayersTurnException(gameId))
        _ <- Either.cond(game.winner.isEmpty, (), GameHasFinishedException(gameId))
        newState <- move.makeAt(game.state)
      } yield newState

      newState <- F.fromEither(either)
      winner = Some(move).collect {
        case PawnMove(pawnPosition) =>
          Some(game.state.players.activePlayer).collect {
            case Player(id, login, _, _, target) if Board.isPawnOnEdge(pawnPosition, target) =>
              User(id, login)
          }
      }.flatten
      newGame <- gameStorage.insert(gameId, newState, winner)
    } yield newGame
  }

  override def gameHistory(gameId: ID[Game], userId: ID[User]): F[List[Game]] = {
    for {
      game <- gameStorage.find(gameId)

      _ <- F.fromEither(
        Either.cond(
          game.state.players.toList.exists(_.id == userId), (), GameInterloperException(userId, gameId)
        )
      )

      gameIds <- gameStorage.gameHistory(gameId)
      history <- if (gameIds.isEmpty) {F.pure(List.empty[Game])} else {
        F.parSequenceN(gameIds.size){
          gameIds.map(gameStorage.find)
        }
      }
    } yield history
  }
}
