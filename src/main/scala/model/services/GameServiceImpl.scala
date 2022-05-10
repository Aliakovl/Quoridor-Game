package model.services

import cats.effect.Async
import cats.implicits._
import model.{GameException, User}
import model.GameException.{GameInterloperException, WrongPlayersTurnException}
import model.game.geometry.Board
import model.game.{Game, Move, Player}
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
        newState <- move.makeAt(game)
      } yield newState

      newState <- F.fromEither(either)
      newGame <- gameStorage.insert(gameId, newState)
    } yield newGame
  }
}
