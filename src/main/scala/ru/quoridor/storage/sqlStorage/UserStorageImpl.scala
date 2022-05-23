package ru.quoridor.storage.sqlStorage

import cats.effect.{Async, Resource}
import doobie.Transactor
import doobie.implicits._
import ru.quoridor.User
import ru.quoridor.game.Game
import ru.quoridor.storage.UserStorage
import ru.utils.Typed.ID


class UserStorageImpl[F[_]: Async](transactor: Resource[F, Transactor[F]]) extends UserStorage[F] {
  override def findByLogin(login: String): F[User] = transactor.use { xa =>
    queries.findUserByLogin(login).transact(xa)
  }

  override def find(id: ID[User]): F[User] = transactor.use { xa =>
    queries.findUserById(id).transact(xa)
  }

  override def insert(login: String): F[User] = transactor.use { xa =>
    queries.registerUser(login).transact(xa)
  }

  override def history(id: ID[User]): F[List[ID[Game]]] = transactor.use { xa =>
    val query = for {
      _ <- queries.findUserById(id)
      userHistory <- queries.findGameLeavesByUserId(id)
    } yield userHistory

    query.transact(xa)
  }
}