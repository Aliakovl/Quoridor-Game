package model.storage

import model.game.{Game, GameState, Player}

import java.util.UUID


trait GameStorage[F[_]] {
  def find(id: UUID): F[Game]

  def insert(previousGameId: UUID, activePlayer: Player, state: GameState): F[Game]

  def create(protoGameId: UUID, activePlayer: Player, state: GameState): F[Game]
}
