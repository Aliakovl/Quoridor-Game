package ru.quoridor.storage.sqlStorage

import doobie.implicits._
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import ru.quoridor.model.GameException.{
  LoginNotFoundException,
  LoginOccupiedException,
  UserNotFoundException
}
import ru.quoridor.model.User
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
          .option
          .transact[Task]
      }
      .someOrFail(LoginNotFoundException(login))
  }

  override def find(id: ID[User]): Task[User] = {
    dataBase
      .transact {
        queries
          .findUserById(id)
          .option
          .transact[Task]
      }
      .someOrFail(UserNotFoundException(id))
  }

  override def insert(user: User): Task[Unit] = {
    dataBase.transact {
      queries
        .registerUser(user)
        .run
        .exceptSomeSqlState { case UNIQUE_VIOLATION =>
          throw LoginOccupiedException(user.login)
        }
        .transact[Task]
    }.unit
  }
}
