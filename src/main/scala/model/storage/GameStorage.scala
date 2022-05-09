package model.storage

import model.game.{Game, GameState, Player}
import utils.Typed.ID


trait GameStorage[F[_]] {
  def find(id: ID[Game]): F[Game]

  def insert(previousGameId: ID[Game], activePlayer: Player, state: GameState): F[Game]

  def create(protoGameId: ID[Game], activePlayer: Player, state: GameState): F[Game]
}
