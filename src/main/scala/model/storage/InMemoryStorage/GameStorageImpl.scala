package model.storage.InMemoryStorage

import model.GameException.{GameNotFoundException, UserNotFoundException}
import model.game.{Game, GameState}
import model.storage.GameStorage

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

class GameStorageImpl extends GameStorage {
  override def insert(id: UUID, activePlayerId: UUID, state: GameState): Future[Game] = {
    state.players.find(_.id == activePlayerId) match {
      case None => Future.failed(UserNotFoundException(activePlayerId))
      case Some(player) =>
        val newId = UUID.randomUUID()
        val newGame = Game(newId, player, state)
        gameStore.update(newId, (newGame, id))
        Future.successful(newGame)
    }
  }

  override def find(id: UUID): Future[Game] = {
    gameStore.get(id) match {
      case Some((game, _)) => Future.successful(game)
      case None => Future.failed(GameNotFoundException(id))
    }
  }

  private val gameStore = TrieMap[UUID, (Game, UUID)]()
}