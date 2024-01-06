package dev.aliakovl.quoridor.dao

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.model.{User, UserWithSecret}
import dev.aliakovl.utils.tagging.ID
import zio.Task

trait UserDao:
  def findByUsername(username: Username): Task[UserWithSecret]

  def findById(id: ID[User]): Task[User]

  def insert(user: UserWithSecret): Task[Unit]
