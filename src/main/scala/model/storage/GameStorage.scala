package model.storage

import model.game.{Game, GameState}

import java.util.UUID
import scala.concurrent.Future

trait GameStorage {
  def find(id: UUID): Future[Game]

  def insert(previousGameId: UUID, activePlayerId: UUID, state: GameState): Future[Game]
}
