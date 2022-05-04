package model.services

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
      // TODO: CHECK PLAYERS QUEUE
      newGame <- move.makeOn(game) match {
        case Some(newState) =>
          val nextPlayerId: UUID = nextPlayer(game)
          gameStorage.insert(gameId, nextPlayerId, newState)
        case None => throw new IllegalArgumentException
      }
    } yield newGame
  }

  def nextPlayer(game: Game): UUID = {
    val players = game.state.players
    val player = game.activePlayer
    val order = Board.playersOrder(players.size)
    val side = offsetMap(order)(player.target)
    players.find(_.target == side).get.id
  }

  def offsetMap[T](list: List[T]): Map[T, T] = {
    list match {
      case x :: xs => list.zip(xs :+ x).toMap
      case _ => Map.empty
    }
  }
}
