package model.services

import model.GameException
import model.GameException.{GameInterloperException, WrongPlayersTurnException}
import model.game.geometry.Board
import model.game.{Game, Move}
import model.storage.{GameStorage, ProtoGameStorage, UserStorage}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class GameServiceImpl(protoGameStorage: ProtoGameStorage,
                      gameStorage: GameStorage,
                      userStorage: UserStorage)(implicit ec: ExecutionContext) extends GameService {
  override def makeMove(gameId: UUID, userId: UUID, move: Move): Future[Game] = {
    for {
      user <- userStorage.find(userId)
      // TODO: CHECK AUTHORIZATION

      game <- gameStorage.find(gameId)

      either = for {
        _ <- Either.cond(game.state.players.exists(_.id == userId), (), GameInterloperException(userId, gameId))
        _ <- Either.cond(game.activePlayer.id == userId, (), WrongPlayersTurnException(gameId))
        newState <- move.makeAt(game)
        nextPlayerId <- nextPlayer(game)
      } yield (nextPlayerId, newState)

      (nextPlayerId, newState) <- Future.fromTry(either.toTry)
      newGame <- gameStorage.insert(gameId, nextPlayerId, newState)
    } yield newGame
  }

  def nextPlayer(game: Game): Either[GameException, UUID] = {
    val players = game.state.players
    val player = game.activePlayer
    for {
      order <- Board.playersOrder(players.size)
      side = offsetMap(order)(player.target)
    } yield players.find(_.target == side).get.id
  }

  def offsetMap[T](list: List[T]): Map[T, T] = {
    list match {
      case x :: xs => list.zip(xs :+ x).toMap
      case _ => Map.empty
    }
  }
}
