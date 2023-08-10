package ru.quoridor.dao

import ru.quoridor.auth.model.Username
import ru.quoridor.model.User
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.model.User.{UserdataWithSecret, Userdata}
import ru.utils.tagging.Id
import zio.{RLayer, Task, ZLayer}

trait UserDao {
  def findByUsername(username: Username): Task[UserdataWithSecret]

  def findById(id: Id[User]): Task[Userdata]

  def insert(user: UserdataWithSecret): Task[Unit]
}

object UserDao {
  val live: RLayer[QuillContext, UserDao] =
    ZLayer.fromFunction(new UserDaoImpl(_))
}
