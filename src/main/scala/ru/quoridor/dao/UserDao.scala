package ru.quoridor.dao

import ru.quoridor.auth.model.Username
import ru.quoridor.model.{User, UserWithSecret}
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.model.User.Userdata
import ru.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait UserDao {
  def findByUsername(username: Username): Task[UserWithSecret]

  def findById(id: ID[User]): Task[Userdata]

  def insert(user: UserWithSecret): Task[Unit]
}

object UserDao {
  val live: RLayer[QuillContext, UserDao] =
    ZLayer.fromFunction(new UserDaoImpl(_))
}
