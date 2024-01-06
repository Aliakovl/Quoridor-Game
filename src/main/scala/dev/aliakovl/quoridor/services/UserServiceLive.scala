package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.auth.HashingService
import dev.aliakovl.quoridor.auth.model.*
import dev.aliakovl.quoridor.model.{User, UserWithSecret}
import dev.aliakovl.quoridor.dao.UserDao
import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.utils.tagging.Tagged.*
import zio.{Task, URLayer, ZLayer}

import java.util.UUID

class UserServiceLive(
    userDao: UserDao,
    hashingService: HashingService
) extends UserService:
  override def getUser(userId: ID[User]): Task[User] = {
    userDao.findById(userId)
  }

  def getUser(username: Username): Task[User] = {
    userDao.findByUsername(username).map {
      case UserWithSecret(id, username, _) =>
        User(id, username)
    }
  }

  override def createUser(credentials: Credentials): Task[User] = {
    for {
      secret <- hashingService.hashPassword(credentials.password)
      userId = UUID.randomUUID().tag[User]
      user = UserWithSecret(userId, credentials.username, secret)
      _ <- userDao.insert(user)
    } yield user.toUser
  }

  override def getUserSecret(username: Username): Task[UserWithSecret] = {
    userDao.findByUsername(username)
  }

object UserServiceLive:
  val live: URLayer[
    UserDao & HashingService,
    UserService
  ] =
    ZLayer.fromFunction(new UserServiceLive(_, _))
