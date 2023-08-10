package ru.quoridor.dao

import org.postgresql.util.PSQLState
import ru.quoridor.auth.model.Username
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.model.GameException._
import ru.quoridor.model.User.{UserdataWithSecret, Userdata}
import ru.quoridor.model.User
import ru.utils.tagging.ID
import zio.{Task, ZIO}

import java.sql.SQLException

class UserDaoImpl(quillContext: QuillContext) extends UserDao {
  import quillContext._

  override def findByUsername(username: Username): Task[UserdataWithSecret] = {
    val findUserByUsername = quote {
      query[dto.Userdata].filter(_.username == lift(username))
    }
    run(findUserByUsername)
      .map(_.headOption)
      .someOrFail(UsernameNotFoundException(username))
      .map { case dto.Userdata(id, username, secret) =>
        UserdataWithSecret(id, username, secret)
      }
  }

  override def findById(id: ID[User]): Task[Userdata] = {
    val findUserById = quote {
      query[dto.Userdata].filter(_.userId == lift(id))
    }
    run(findUserById)
      .map(_.headOption)
      .someOrFail(UserNotFoundException(id))
      .map { case dto.Userdata(id, username, _) =>
        User.Userdata(id, username)
      }
  }

  override def insert(user: UserdataWithSecret): Task[Unit] = {
    val registerUser = quote {
      query[dto.Userdata].insert(
        _.userId -> lift(user.id),
        _.username -> lift(user.username),
        _.userSecret -> lift(user.userSecret)
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
          ZIO.fail(UsernameOccupiedException(user.username))
      }
  }
}
