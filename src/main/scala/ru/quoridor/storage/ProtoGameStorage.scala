package ru.quoridor.storage

import ru.quoridor.model.ProtoGame
import ru.quoridor.model.game.geometry.Side
import ru.quoridor.storage.sqlStorage.ProtoGameStorageImpl
import zio.{RLayer, Task, ZLayer}

import java.util.UUID

trait ProtoGameStorage {
  def find(gameId: UUID): Task[ProtoGame]

  def insert(userId: UUID): Task[ProtoGame]

  def update(gameId: UUID, userId: UUID, target: Side): Task[ProtoGame]
}

object ProtoGameStorage {
  val live: RLayer[DataBase, ProtoGameStorage] =
    ZLayer.fromFunction(ProtoGameStorageImpl.apply _)
}
