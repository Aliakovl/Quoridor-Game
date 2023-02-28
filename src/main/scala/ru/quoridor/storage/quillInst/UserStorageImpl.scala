package ru.quoridor.storage.quillInst

import io.getquill.{CompositeNamingStrategy2, Escape, SnakeCase}
import io.getquill.jdbczio.Quill
import org.postgresql.util.PSQLState
import ru.quoridor.model.GameException.{
  LoginNotFoundException,
  LoginOccupiedException,
  UserNotFoundException
}
import ru.quoridor.model.User
import ru.quoridor.storage.{UserStorage, dto}
import ru.utils.tagging.ID
import zio.{Task, ZIO}

import java.sql.SQLException

class UserStorageImpl(
    quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, Escape]]
) extends UserStorage {
  import quill._

  override def findByLogin(login: String): Task[User] = {
    val findUserByLogin = quote {
      query[dto.`User`].filter(_.login == lift(login))
    }
    run(findUserByLogin)
      .map(_.headOption)
      .someOrFail(LoginNotFoundException(login))
      .map { case dto.User(id, login) =>
        User(id, login)
      }
  }

  override def findById(id: ID[User]): Task[User] = {
    val findUserById = quote {
      query[dto.User].filter(_.userId == lift(id))
    }
    run(findUserById)
      .map(_.headOption)
      .someOrFail(UserNotFoundException(id))
      .map { case dto.User(id, login) =>
        User(id, login)
      }
  }

  override def insert(user: User): Task[Unit] = {
    val registerUser = quote {
      query[dto.User].insert(
        _.userId -> lift(user.id),
        _.login -> lift(user.login)
      )
    }
    run(registerUser)
      .reject {
        case x if x != 1 => new Throwable("Cannot insert user to database")
      }
      .unit
      .catchSome {
        case x: SQLException
            if x.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
          ZIO.fail(LoginOccupiedException(user.login))
      }
  }

}
