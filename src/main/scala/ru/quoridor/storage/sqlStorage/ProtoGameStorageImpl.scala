package ru.quoridor.storage.sqlStorage

import doobie.implicits._
import ru.quoridor.model.{ProtoGame, User}
import ru.quoridor.model.game.Game
import ru.quoridor.model.game.geometry.Side
import ru.quoridor.storage.{DataBase, ProtoGameStorage}
import ru.utils.tagging.ID
import zio.Task
import zio.interop.catz._

class ProtoGameStorageImpl(dataBase: DataBase) extends ProtoGameStorage {
  override def find(gameId: ID[Game]): Task[ProtoGame] = {
    dataBase.transact {
      queries
        .findProtoGameByGameId(gameId)
        .transact[Task]
    }
  }

  override def insert(gameId: ID[Game], userId: ID[User]): Task[Unit] = {
    dataBase.transact {
      queries
        .createProtoGameByUser(gameId, userId)
        .transact[Task]
    }
  }

  override def update(
      gameId: ID[Game],
      userId: ID[User],
      target: Side
  ): Task[ProtoGame] = {
    val query = for {
      _ <- queries.findUserById(userId)
      _ <- queries.addUserIntoProtoGame(gameId, userId, target)
      protoGame <- queries.findProtoGameByGameId(gameId)
    } yield protoGame

    dataBase.transact(query.transact[Task])
  }
}
