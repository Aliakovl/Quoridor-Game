package ru.quoridor.storage.sqlStorage

import doobie.implicits._
import doobie.postgres.sqlstate.class23.{
  FOREIGN_KEY_VIOLATION,
  UNIQUE_VIOLATION
}
import ru.quoridor.model.GameException.{
  GameNotFoundException,
  SamePlayerException
}
import ru.quoridor.model.{ProtoGame, ProtoPlayers, User}
import ru.quoridor.model.game.Game
import ru.quoridor.model.game.geometry.Side
import ru.quoridor.storage.{DataBase, ProtoGameStorage}
import ru.utils.tagging.ID
import zio.Task
import zio.interop.catz._

class ProtoGameStorageImpl(dataBase: DataBase) extends ProtoGameStorage {
  override def find(gameId: ID[Game]): Task[ProtoGame] = {
    dataBase
      .transact {
        queries
          .findProtoGameByGameId(gameId)
          .to[List]
          .transact[Task]
      }
      .map {
        case Nil => throw GameNotFoundException(gameId)
        case creator :: guests =>
          ProtoGame(gameId, ProtoPlayers(creator, guests))
      }
  }

  override def insert(gameId: ID[Game], userId: ID[User]): Task[Unit] = {
    dataBase.transact {
      queries
        .createProtoGameByUser(gameId, userId)
        .transact[Task]
    }
  }

  override def addPlayer(
      gameId: ID[Game],
      userId: ID[User],
      target: Side
  ): Task[Unit] = {
    dataBase.transact {
      queries
        .addUserIntoProtoGame(gameId, userId, target)
        .run
        .exceptSomeSqlState {
          case UNIQUE_VIOLATION      => throw SamePlayerException(userId, gameId)
          case FOREIGN_KEY_VIOLATION => throw GameNotFoundException(gameId)
        }
        .transact[Task]
    }.unit
  }
}
