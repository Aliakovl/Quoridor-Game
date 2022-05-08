package model.storage.sqlStorage

import cats.effect.Async
import doobie.implicits._
import doobie.util.transactor.Transactor
import model.{ProtoGame, ProtoPlayer, User}
import model.game.geometry.Side
import model.game.geometry.Side.North
import model.storage.ProtoGameStorage
import java.util.UUID


class ProtoGameStorageImpl[F[_]: Async](implicit xa: Transactor[F]) extends ProtoGameStorage[F] {
  override def find(gameId: UUID): F[ProtoGame] = {
    queries.findProtoGameById(gameId).transact(xa)
  }

  override def insert(userId: UUID): F[ProtoGame] = {
    lazy val gameId = UUID.randomUUID()
    val target = North
    val query = for {
      user <- queries.findUserById(userId)
      _ <- queries.createProtoGameByUser(gameId, userId)
      protoPlayer = user match {
        case User(id, login) => ProtoPlayer(id, login, target)
      }
    } yield ProtoGame(gameId, Seq(protoPlayer))

    query.transact(xa)
  }

  override def update(gameId: UUID, userId: UUID, target: Side): F[ProtoGame] = {
    val query = for {
      _ <- queries.findUserById(userId)
      _ <- queries.addUserIntoProtoGame(gameId, userId, target)
      protoGame <- queries.findProtoGameById(gameId)
    } yield protoGame

    query.transact(xa)
  }
}