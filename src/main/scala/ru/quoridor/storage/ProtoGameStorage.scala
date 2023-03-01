package ru.quoridor.storage

import ru.quoridor.model.game.Game
import ru.quoridor.model.{ProtoGame, User}
import ru.quoridor.model.game.geometry.Side
import ru.quoridor.storage.quillInst.{ProtoGameStorageImpl, QuillContext}
import ru.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait ProtoGameStorage {
  def find(gameId: ID[Game]): Task[ProtoGame]

  def insert(gameId: ID[Game], userId: ID[User], target: Side): Task[Unit]

  def addPlayer(gameId: ID[Game], userId: ID[User], target: Side): Task[Unit]
}

object ProtoGameStorage {
  val live: RLayer[QuillContext, ProtoGameStorage] =
    ZLayer.fromFunction(new ProtoGameStorageImpl(_))
}
