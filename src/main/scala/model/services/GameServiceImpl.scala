package model.services

import cats.effect.Async
import cats.implicits._
import model.GameException
import model.GameException.{GameInterloperException, WrongPlayersTurnException}
import model.game.geometry.Board
import model.game.{Game, Move, Player}
import model.storage.{GameStorage, ProtoGameStorage, UserStorage}

import java.util.UUID

class GameServiceImpl[F[_]](protoGameStorage: ProtoGameStorage[F],
                      gameStorage: GameStorage[F],
                      userStorage: UserStorage[F])(implicit F: Async[F]) extends GameService[F] {
  override def makeMove(gameId: UUID, userId: UUID, move: Move): F[Game] = {
    for {
      user <- userStorage.find(userId)
      game <- gameStorage.find(gameId)

      either = for {
        _ <- Either.cond(game.state.players.exists(_.id == userId), (), GameInterloperException(userId, gameId))
        _ <- Either.cond(game.activePlayer.id == userId, (), WrongPlayersTurnException(gameId))
        newState <- move.makeAt(game)
        nextPlayer <- nextPlayer(game)
      } yield (nextPlayer, newState)

      f <- F.fromTry(either.toTry)
      (nextPlayer, newState) = f
      newGame <- gameStorage.insert(gameId, nextPlayer, newState)
    } yield newGame
  }

  def nextPlayer(game: Game): Either[GameException, Player] = {
    val players = game.state.players
    val player = game.activePlayer
    for {
      order <- Board.playersOrder(players.size)
      side = offsetMap(order)(player.target)
    } yield players.find(_.target == side).get
  }

  def offsetMap[T](list: List[T]): Map[T, T] = {
    list match {
      case x :: xs => list.zip(xs :+ x).toMap
      case _ => Map.empty
    }
  }
}
