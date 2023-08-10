package ru.quoridor.services

import ru.quoridor.auth.HashingService
import ru.quoridor.auth.model.{Credentials, Password, UserSecret, Username}
import ru.quoridor.dao.UserDao
import ru.quoridor.model.User.{UserdataWithSecret, Userdata}
import ru.quoridor.model.User
import ru.utils.tagging.ID
import zio._

trait UserService {
  def getUser(userId: ID[User]): Task[Userdata]

  def getUser(username: Username): Task[Userdata]

  def createUser(credentials: Credentials): Task[Userdata]

  def getUserSecret(username: Username): Task[UserdataWithSecret]
}

object UserService {
  val live: URLayer[
    UserDao with HashingService[Password, UserSecret],
    UserService
  ] =
    ZLayer.fromFunction(new UserServiceImpl(_, _))

  def getUser(userId: ID[User]): RIO[UserService, Userdata] =
    ZIO.serviceWithZIO[UserService](_.getUser(userId))

  def getUser(username: Username): RIO[UserService, Userdata] =
    ZIO.serviceWithZIO[UserService](_.getUser(username))

  def createUser(credentials: Credentials): RIO[UserService, Userdata] =
    ZIO.serviceWithZIO[UserService](_.createUser(credentials))

  def getUserSecret(username: Username): RIO[UserService, UserdataWithSecret] =
    ZIO.serviceWithZIO[UserService](_.getUserSecret(username))
}
