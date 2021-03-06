package ru.quoridor.storage.sqlStorage

import cats.effect.{Async, Resource}
import doobie.Transactor
import doobie.implicits._
import ru.quoridor
import ru.quoridor.{ProtoGame, ProtoPlayers, User}
import ru.quoridor.game.Game
import ru.quoridor.game.geometry.Side.North
import ru.quoridor.game.geometry.Side
import ru.quoridor.storage.ProtoGameStorage
import ru.utils.Typed.ID
import ru.utils.Typed.Implicits._

import java.util.UUID


class ProtoGameStorageImpl[F[_]: Async](transactor: Resource[F, Transactor[F]]) extends ProtoGameStorage[F] {
  override def find(gameId: ID[Game]): F[ProtoGame] = transactor.use { xa =>
    queries.findProtoGameByGameId(gameId).transact(xa)
  }

  override def insert(userId: ID[User]): F[ProtoGame] = transactor.use { xa =>
    lazy val gameId = UUID.randomUUID().typed[Game]
    val target = North
    val query = for {
      user <- queries.findUserById(userId)
      _ <- queries.createProtoGameByUser(gameId, userId)
      protoPlayer = user match {
        case User(id, login) => quoridor.ProtoPlayer(id, login, target)
      }
    } yield quoridor.ProtoGame(gameId, ProtoPlayers(protoPlayer, List.empty))

    query.transact(xa)
  }

  override def update(gameId: ID[Game], userId: ID[User], target: Side): F[ProtoGame] = transactor.use { xa =>
    val query = for {
      _ <- queries.findUserById(userId)
      _ <- queries.addUserIntoProtoGame(gameId, userId, target)
      protoGame <- queries.findProtoGameByGameId(gameId)
    } yield protoGame

    query.transact(xa)
  }
}