package model.services

import cats.effect.Async
import cats.implicits._
import model.User
import model.GameException.{GameInterloperException, WrongPlayersTurnException}
import model.game.{Game, Move}
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
        newState <- move.makeAt(game.state)
      } yield newState

      newState <- F.fromEither(either)
      newGame <- gameStorage.insert(gameId, newState)
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
