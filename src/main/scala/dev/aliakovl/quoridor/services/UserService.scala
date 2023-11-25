package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.auth.HashingService
import dev.aliakovl.quoridor.auth.model.{
  Credentials,
  Password,
  UserSecret,
  Username
}
import dev.aliakovl.quoridor.dao.UserDao
import dev.aliakovl.quoridor.model.{User, UserWithSecret}
import dev.aliakovl.utils.tagging.ID
import zio.*

trait UserService {
  def getUser(userId: ID[User]): Task[User]

  def getUser(username: Username): Task[User]

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

  def getUser(username: Username): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.getUser(username))

  def createUser(credentials: Credentials): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.createUser(credentials))

  def getUserSecret(username: Username): RIO[UserService, UserWithSecret] =
    ZIO.serviceWithZIO[UserService](_.getUserSecret(username))
}
