package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.auth.model.*
import dev.aliakovl.quoridor.model.{User, UserWithSecret}
import dev.aliakovl.utils.tagging.ID
import zio.*

trait UserService:
  def getUser(userId: ID[User]): Task[User]

  def getUser(username: Username): Task[User]

  def createUser(credentials: Credentials): Task[User]

  def getUserSecret(username: Username): Task[UserWithSecret]
