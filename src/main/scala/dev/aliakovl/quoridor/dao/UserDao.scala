package dev.aliakovl.quoridor.dao

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.model.{User, UserWithSecret}
import dev.aliakovl.quoridor.dao.quill.QuillContext
import dev.aliakovl.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait UserDao:
  def findByUsername(username: Username): Task[UserWithSecret]

  def findById(id: ID[User]): Task[User]

  def insert(user: UserWithSecret): Task[Unit]

object UserDao:
  val live: RLayer[QuillContext, UserDao] =
    ZLayer.fromFunction(new UserDaoLive(_))
