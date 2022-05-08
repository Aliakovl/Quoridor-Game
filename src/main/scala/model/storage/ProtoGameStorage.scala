package model.storage

import model.ProtoGame
import model.game.geometry.Side

import java.util.UUID


trait ProtoGameStorage[F[_]] {
  def find(gameId: UUID): F[ProtoGame]

  def insert(userId: UUID): F[ProtoGame]

  def update(gameId: UUID, userId: UUID, target: Side): F[ProtoGame]
}