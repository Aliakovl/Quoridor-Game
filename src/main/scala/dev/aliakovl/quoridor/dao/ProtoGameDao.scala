package dev.aliakovl.quoridor.dao

import dev.aliakovl.quoridor.engine.Game
import dev.aliakovl.quoridor.model.{ProtoGame, User}
import dev.aliakovl.quoridor.engine.geometry.Side
import dev.aliakovl.quoridor.dao.quill.QuillContext
import dev.aliakovl.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait ProtoGameDao:
  def find(gameId: ID[Game]): Task[ProtoGame]

  def insert(gameId: ID[Game], userId: ID[User], target: Side): Task[Unit]

  def addPlayer(gameId: ID[Game], userId: ID[User], target: Side): Task[Unit]

object ProtoGameDao:
  val live: RLayer[QuillContext, ProtoGameDao] =
    ZLayer.fromFunction(new ProtoGameDaoImpl(_))
