package ru.quoridor.storage

import cats.effect.Resource
import doobie.Transactor
import ru.quoridor.{ProtoGame, User}
import ru.quoridor.game.Game
import ru.quoridor.game.geometry.Side
import ru.quoridor.storage.sqlStorage.ProtoGameStorageImpl
import ru.utils.Typed.ID
import zio.{Task, ZLayer}

trait ProtoGameStorage {
  def find(gameId: ID[Game]): Task[ProtoGame]

  def insert(userId: ID[User]): Task[ProtoGame]

  def update(gameId: ID[Game], userId: ID[User], target: Side): Task[ProtoGame]
}

object ProtoGameStorage {
  val live: ZLayer[
    Resource[Task, Transactor[Task]],
    Nothing,
    ProtoGameStorage
  ] = ZLayer.fromFunction(ProtoGameStorageImpl.apply _)
}
