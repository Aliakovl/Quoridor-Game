package ru.quoridor.services

import ru.quoridor.auth.HashingService
import ru.quoridor.auth.model.{Credentials, Password, UserSecret, Username}
import ru.quoridor.dao.UserDao
import ru.quoridor.model.{User, UserWithSecret}
import zio._

trait UserService {
  def findUser(username: Username): Task[User]

  def createUser(credentials: Credentials): Task[User]

  def getUserSecret(username: Username): Task[UserWithSecret]
}

object UserService {
  val live: URLayer[
    UserDao with HashingService[Password, UserSecret],
    UserService
  ] =
    ZLayer.fromFunction(new UserServiceImpl(_, _))

  def findUser(username: Username): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.findUser(username))

  def createUser(credentials: Credentials): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.createUser(credentials))

  def getUserSecret(username: Username): RIO[UserService, UserWithSecret] =
    ZIO.serviceWithZIO[UserService](_.getUserSecret(username))
}
