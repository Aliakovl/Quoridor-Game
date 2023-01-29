package ru.quoridor.storage.sqlStorage

import cats.effect.Resource
import doobie.Transactor
import doobie.implicits._
import ru.quoridor.User
import ru.quoridor.game.Game
import ru.quoridor.storage.UserStorage
import ru.utils.Typed.ID
import zio.Task
import zio.interop.catz._

class UserStorageImpl(transactor: Resource[Task, Transactor[Task]])
    extends UserStorage {
  override def findByLogin(login: String): Task[User] = transactor.use { xa =>
    queries.findUserByLogin(login).transact(xa)
  }

  override def find(id: ID[User]): Task[User] = transactor.use { xa =>
    queries.findUserById(id).transact(xa)
  }

  override def insert(login: String): Task[User] = transactor.use { xa =>
    queries.registerUser(login).transact(xa)
  }

  override def history(id: ID[User]): Task[List[ID[Game]]] = transactor.use {
    xa =>
      val query = for {
        _ <- queries.findUserById(id)
        userHistory <- queries.findGameLeavesByUserId(id)
      } yield userHistory

      query.transact(xa)
  }
}
