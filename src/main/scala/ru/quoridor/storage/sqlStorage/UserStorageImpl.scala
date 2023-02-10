package ru.quoridor.storage.sqlStorage

import doobie.implicits._
import ru.quoridor.model.GameException.{
  LoginNotFoundException,
  UserNotFoundException
}
import ru.quoridor.model.User
import ru.quoridor.model.game.Game
import ru.quoridor.storage.{DataBase, UserStorage}
import ru.utils.tagging.ID
import zio.Task
import zio.interop.catz._

class UserStorageImpl(dataBase: DataBase) extends UserStorage {
  override def findByLogin(login: String): Task[User] = {
    dataBase
      .transact {
        queries
          .findUserByLogin(login)
          .transact[Task]
      }
      .someOrFail(LoginNotFoundException(login))
  }

  override def find(id: ID[User]): Task[User] = {
    dataBase
      .transact {
        queries
          .findUserById(id)
          .transact[Task]
      }
      .someOrFail(UserNotFoundException(id))
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
