package dev.aliakovl.quoridor.dao

import io.getquill.*
import org.postgresql.util.PSQLState
import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.dao.quill.QuillContext
import dev.aliakovl.quoridor.model.GameException.{
  UserNotFoundException,
  UsernameNotFoundException,
  UsernameOccupiedException
}
import dev.aliakovl.quoridor.model.{User, UserWithSecret}
import dev.aliakovl.utils.tagging.ID
import zio.{Task, ZIO}

import java.sql.SQLException

class UserDaoImpl(quillContext: QuillContext) extends UserDao:

  import quillContext.*
  import quillContext.given

  override def findByUsername(username: Username): Task[UserWithSecret] = {
    val findUserByUsername = run {
      query[dto.Userdata].filter(_.username == lift(username))
    }
    findUserByUsername
      .map(_.headOption)
      .someOrFail(UsernameNotFoundException(username))
      .map { case dto.Userdata(id, username, secret) =>
        UserWithSecret(id, username, secret)
      }
  }

  override def findById(id: ID[User]): Task[User] = {
    val findUserById = run {
      query[dto.Userdata].filter(_.userId == lift(id))
    }
    findUserById
      .map(_.headOption)
      .someOrFail(UserNotFoundException(id))
      .map { case dto.Userdata(id, username, _) =>
        User(id, username)
      }
  }

  override def insert(user: UserWithSecret): Task[Unit] = {
    val registerUser = run {
      query[dto.Userdata].insert(
        _.userId -> lift(user.id),
        _.username -> lift(user.username),
        _.userSecret -> lift(user.userSecret)
      )
    }
    registerUser
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
