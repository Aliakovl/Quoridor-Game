package ru.quoridor.storage.sqlStorage

import doobie.implicits._
import ru.quoridor
import ru.quoridor.{ProtoGame, ProtoPlayers, User}
import ru.quoridor.game.Game
import ru.quoridor.game.geometry.Side.North
import ru.quoridor.game.geometry.Side
import ru.quoridor.storage.{DataBase, ProtoGameStorage}
import ru.utils.Typed.ID
import ru.utils.Typed.Implicits._
import zio.Task
import zio.interop.catz._

import java.util.UUID

class ProtoGameStorageImpl(dataBase: DataBase) extends ProtoGameStorage {
  override def find(gameId: ID[Game]): Task[ProtoGame] =
    dataBase.transact(queries.findProtoGameByGameId(gameId).transact[Task])

  override def insert(userId: ID[User]): Task[ProtoGame] = {
    lazy val gameId = UUID.randomUUID().typed[Game]
    val target = North
    val query = for {
      user <- queries.findUserById(userId)
      _ <- queries.createProtoGameByUser(gameId, userId)
      protoPlayer = user match {
        case User(id, login) => quoridor.ProtoPlayer(id, login, target)
      }
    } yield quoridor.ProtoGame(gameId, ProtoPlayers(protoPlayer, List.empty))

    dataBase.transact(query.transact[Task])
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

object ProtoGameStorageImpl {
  def apply(dataBase: DataBase): ProtoGameStorageImpl =
    new ProtoGameStorageImpl(dataBase)
}
