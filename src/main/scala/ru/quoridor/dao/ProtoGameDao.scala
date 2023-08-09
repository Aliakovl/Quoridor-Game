package ru.quoridor.dao

import ru.quoridor.model.game.Game
import ru.quoridor.model.ProtoGame
import ru.quoridor.model.game.geometry.Side
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.model.User
import ru.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait ProtoGameDao {
  def find(gameId: ID[Game]): Task[ProtoGame]

  def insert(gameId: ID[Game], userId: ID[User], target: Side): Task[Unit]

  def addPlayer(gameId: ID[Game], userId: ID[User], target: Side): Task[Unit]
}

object ProtoGameDao {
  val live: RLayer[QuillContext, ProtoGameDao] =
    ZLayer.fromFunction(new ProtoGameDaoImpl(_))
}
