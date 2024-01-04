package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.auth.HashingService
import dev.aliakovl.quoridor.auth.model.*
import dev.aliakovl.quoridor.dao.UserDao
import dev.aliakovl.quoridor.model.{User, UserWithSecret}
import dev.aliakovl.utils.tagging.ID
import zio.*

trait UserService:
  def getUser(userId: ID[User]): Task[User]

  def getUser(username: Username): Task[User]

  def createUser(credentials: Credentials): Task[User]

  def getUserSecret(username: Username): Task[UserWithSecret]

object UserService:
  val live: URLayer[
    UserDao & HashingService,
    UserService
  ] =
    ZLayer.fromFunction(new UserServiceLive(_, _))
