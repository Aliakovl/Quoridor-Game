package model.storage

import model.game.{Game, State}
import utils.Typed.ID


trait GameStorage[F[_]] {
  def find(id: ID[Game]): F[Game]

  def insert(previousGameId: ID[Game], state: State): F[Game]

  def create(protoGameId: ID[Game], state: State): F[Game]

  def exists(gameId: ID[Game]): F[Boolean]

  def gameHistory(gameId: ID[Game]): F[List[ID[Game]]]
}
