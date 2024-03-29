package dev.aliakovl.quoridor.dao

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.dao.quill.QuillContext
import dev.aliakovl.quoridor.GameException.{
  UserNotFoundException,
  UsernameNotFoundException,
  UsernameOccupiedException,
  UsersNotFoundException
}
import dev.aliakovl.quoridor.model.{User, UserWithSecret}
import dev.aliakovl.utils.tagging.ID
import org.postgresql.util.PSQLState
import io.getquill.*
import zio.{IO, RLayer, Task, ZIO, ZLayer}

import java.sql.SQLException

class UserDaoLive(quillContext: QuillContext) extends UserDao:

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

  override def findById(id: ID[User]): IO[UserNotFoundException, User] = {
    val findUserById = run {
      query[dto.Userdata].filter(_.userId == lift(id))
    }
    findUserById
      .map(_.headOption)
      .someOrFail(UserNotFoundException(id))
      .map { case dto.Userdata(id, username, _) =>
        User(id, username)
      }
      .orDie
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

  override def findUsers(
      ids: Seq[ID[User]]
  ): IO[UsersNotFoundException, Map[ID[User], User]] = run {
    query[dto.Userdata]
      .filter(u => liftQuery(ids).contains(u.userId))
      .map { u => (u.userId, u.username) }
  }.map(_.map((id, username) => User(id, username)))
    .filterOrFail(_.size == ids.size)(UsersNotFoundException)
    .map(_.map(user => (user.id, user)).toMap)
    .orDie

object UserDaoLive:
  val live: RLayer[QuillContext, UserDao] =
    ZLayer.fromFunction(new UserDaoLive(_))
