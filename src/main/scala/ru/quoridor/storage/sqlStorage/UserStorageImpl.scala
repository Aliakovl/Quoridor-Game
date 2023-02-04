package ru.quoridor.storage.sqlStorage

import doobie.implicits._
import ru.quoridor.User
import ru.quoridor.game.Game
import ru.quoridor.storage.{DataBase, UserStorage}
import ru.utils.Typed.ID
import zio.Task
import zio.interop.catz._

class UserStorageImpl(dataBase: DataBase) extends UserStorage {
  override def findByLogin(login: String): Task[User] = {
    dataBase.transact(queries.findUserByLogin(login).transact[Task])
  }

  override def find(id: ID[User]): Task[User] = {
    dataBase.transact(queries.findUserById(id).transact[Task])
  }

  override def insert(login: String): Task[User] = {
    dataBase.transact(queries.registerUser(login).transact[Task])
  }

  override def history(id: ID[User]): Task[List[ID[Game]]] = {
    val query = for {
      _ <- queries.findUserById(id)
      userHistory <- queries.findGameLeavesByUserId(id)
    } yield userHistory

    dataBase.transact(query.transact[Task])
  }
}

object UserStorageImpl {
  def apply(dataBase: DataBase): UserStorageImpl =
    new UserStorageImpl(dataBase)
}
