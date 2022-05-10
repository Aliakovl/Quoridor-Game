package model.storage.sqlStorage

import cats.effect.Async
import doobie.implicits._
import doobie.util.transactor.Transactor
import model.game.Game
import model.{ProtoGame, ProtoPlayer, ProtoPlayers, User}
import model.game.geometry.Side
import model.game.geometry.Side.North
import model.storage.ProtoGameStorage
import utils.Typed.ID
import utils.Typed.Implicits._
import java.util.UUID


class ProtoGameStorageImpl[F[_]: Async](implicit xa: Transactor[F]) extends ProtoGameStorage[F] {
  override def find(gameId: ID[Game]): F[ProtoGame] = {
    queries.findProtoGameByGameId(gameId).transact(xa)
  }

  override def insert(userId: ID[User]): F[ProtoGame] = {
    lazy val gameId = UUID.randomUUID().typed[Game]
    val target = North // remove
    val query = for {
      user <- queries.findUserById(userId)
      _ <- queries.createProtoGameByUser(gameId, userId)
      protoPlayer = user match {
        case User(id, login) => ProtoPlayer(id, login, target)
      }
    } yield ProtoGame(gameId, ProtoPlayers(protoPlayer, List.empty))

    query.transact(xa)
  }

  override def update(gameId: ID[Game], userId: ID[User], target: Side): F[ProtoGame] = {
    val query = for {
      _ <- queries.findUserById(userId)
      _ <- queries.addUserIntoProtoGame(gameId, userId, target)
      protoGame <- queries.findProtoGameByGameId(gameId)
    } yield protoGame

    query.transact(xa)
  }
}