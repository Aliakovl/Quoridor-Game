package ru.quoridor.storage

import ru.quoridor.model.game.Game
import ru.quoridor.model.{ProtoGame, User}
import ru.quoridor.model.game.geometry.Side
import ru.quoridor.storage.sqlStorage.ProtoGameStorageImpl
import ru.utils.Tagged.ID
import zio.{RLayer, Task, ZLayer}

trait ProtoGameStorage {
  def find(gameId: ID[Game]): Task[ProtoGame]

  def insert(userId: ID[User]): Task[ProtoGame]

  def update(gameId: ID[Game], userId: ID[User], target: Side): Task[ProtoGame]
}

object ProtoGameStorage {
  val live: RLayer[DataBase, ProtoGameStorage] =
    ZLayer.fromFunction(ProtoGameStorageImpl.apply _)
}
