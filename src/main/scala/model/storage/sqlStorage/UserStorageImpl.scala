package model.storage.sqlStorage

import cats.effect.Async
import doobie.implicits._
import doobie.util.transactor.Transactor
import model.game.Game
import model.User
import model.storage.UserStorage
import utils.Typed.ID


class UserStorageImpl[F[_]: Async](implicit xa: Transactor[F]) extends UserStorage[F] {
  override def findByLogin(login: String): F[User] = {
    queries.findUserByLogin(login).transact(xa)
  }

  override def find(id: ID[User]): F[User] = {
    queries.findUserById(id).transact(xa)
  }

  override def insert(login: String): F[User] = {
    queries.registerUser(login).transact(xa)
  }

  override def history(id: ID[User]): F[List[ID[Game]]] = {
    val query = for {
      _ <- queries.findUserById(id)
      userHistory <- queries.findGameLeavesByUserId(id)
    } yield userHistory

    query.transact(xa)
  }
}