package model.services

import cats.effect.Async
import cats.implicits._
import model.User
import model.GameException.{GameHasFinishedException, GameInterloperException, WrongPlayersTurnException}
import model.game.geometry.Board
import model.game.{Game, Move, PawnMove, Player}
import model.storage.{GameStorage, ProtoGameStorage, UserStorage}
import utils.Typed.ID


class GameServiceImpl[F[_]](protoGameStorage: ProtoGameStorage[F],
                      gameStorage: GameStorage[F],
                      userStorage: UserStorage[F])(implicit F: Async[F]) extends GameService[F] {
  override def makeMove(gameId: ID[Game], userId: ID[User], move: Move): F[Game] = {
    for {
      user <- userStorage.find(userId)
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
      user <- userStorage.find(userId)
      game <- gameStorage.find(gameId)

      _ <- F.fromEither(
        Either.cond(
          game.state.players.toList.exists(_.id == userId), (), GameInterloperException(userId, gameId)
        )
      )

      gameIds <- gameStorage.gameHistory(gameId)
      history <- F.parSequenceN(gameIds.size){
        gameIds.map(gameStorage.find)
      }
    } yield history
  }
}
