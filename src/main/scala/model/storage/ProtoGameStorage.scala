package model.storage

import model.game.Game
import model.{ProtoGame, User}
import model.game.geometry.Side
import utils.Typed.ID


trait ProtoGameStorage[F[_]] {
  def find(gameId: ID[Game]): F[ProtoGame]

  def insert(userId: ID[User]): F[ProtoGame]

  def update(gameId: ID[Game], userId: ID[User], target: Side): F[ProtoGame]
}