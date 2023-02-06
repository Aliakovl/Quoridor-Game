package ru.quoridor.storage.sqlStorage

import doobie.implicits._
import ru.quoridor.model.User
import ru.quoridor.storage.{DataBase, UserStorage}
import zio.Task
import zio.interop.catz._

import java.util.UUID

class UserStorageImpl(dataBase: DataBase) extends UserStorage {
  override def findByLogin(login: String): Task[User] = {
    dataBase.transact(queries.findUserByLogin(login).transact[Task])
  }

  override def find(userId: UUID): Task[User] = {
    dataBase.transact(queries.findUserById(userId).transact[Task])
  }

  override def insert(login: String): Task[User] = {
    dataBase.transact(queries.registerUser(login).transact[Task])
  }

  override def history(userId: UUID): Task[List[UUID]] = {
    val query = for {
      _ <- queries.findUserById(userId)
      userHistory <- queries.findGameLeavesByUserId(userId)
    } yield userHistory

    dataBase.transact(query.transact[Task])
  }
}

object UserStorageImpl {
  def apply(dataBase: DataBase): UserStorageImpl =
    new UserStorageImpl(dataBase)
}
