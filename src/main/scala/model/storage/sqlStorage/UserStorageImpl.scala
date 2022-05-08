package model.storage.sqlStorage

import cats.effect.Async
import doobie.implicits._
import doobie.util.transactor.Transactor
import model.User
import model.storage.UserStorage
import java.util.UUID


class UserStorageImpl[F[_]: Async](implicit xa: Transactor[F]) extends UserStorage[F] {
  override def find(id: UUID): F[User] = {
    queries.findUserById(id).transact(xa)
  }

  override def insert(login: String): F[User] = {
    queries.registerUser(login).transact(xa)
  }
}