package ru.quoridor.services

import ru.quoridor.auth.HashingService
import ru.quoridor.auth.model.{Credentials, Password, UserSecret, Username}
import ru.quoridor.dao.UserDao
import ru.quoridor.model.{User, UserWithSecret}
import ru.utils.tagging.ID
import zio._

trait UserService {
  def getUser(userId: ID[User]): Task[User]

  def createUser(credentials: Credentials): Task[User]

  def getUserSecret(username: Username): Task[UserWithSecret]
}

object UserService {
  val live: URLayer[
    UserDao with HashingService[Password, UserSecret],
    UserService
  ] =
    ZLayer.fromFunction(new UserServiceImpl(_, _))

  def getUser(userId: ID[User]): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.getUser(userId))

  def createUser(credentials: Credentials): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.createUser(credentials))

  def getUserSecret(username: Username): RIO[UserService, UserWithSecret] =
    ZIO.serviceWithZIO[UserService](_.getUserSecret(username))
}
