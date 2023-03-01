package ru.quoridor.dao

import ru.quoridor.model.User
import ru.quoridor.dao.quill.QuillContext
import ru.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait UserDao {
  def findByLogin(login: String): Task[User]

  def findById(id: ID[User]): Task[User]

  def insert(user: User): Task[Unit]
}

object UserDao {
  val live: RLayer[QuillContext, UserDao] =
    ZLayer.fromFunction(new UserDaoImpl(_))
}
