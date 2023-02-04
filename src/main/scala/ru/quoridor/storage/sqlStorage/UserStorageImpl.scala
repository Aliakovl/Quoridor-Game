package ru.quoridor.storage.sqlStorage

import doobie.Transactor
import doobie.implicits._
import ru.quoridor.User
import ru.quoridor.game.Game
import ru.quoridor.storage.UserStorage
import ru.utils.Typed.ID
import zio.Task
import zio.interop.catz._

class UserStorageImpl(transactor: Transactor[Task]) extends UserStorage {
  override def findByLogin(login: String): Task[User] = {
    queries.findUserByLogin(login).transact(transactor)
  }

  override def find(id: ID[User]): Task[User] = {
    queries.findUserById(id).transact(transactor)
  }

  override def insert(login: String): Task[User] = {
    queries.registerUser(login).transact(transactor)
  }

  override def history(id: ID[User]): Task[List[ID[Game]]] = {
    val query = for {
      _ <- queries.findUserById(id)
      userHistory <- queries.findGameLeavesByUserId(id)
    } yield userHistory

    query.transact(transactor)
  }
}

object UserStorageImpl {
  def apply(transactor: Transactor[Task]): UserStorageImpl =
    new UserStorageImpl(transactor)
}
