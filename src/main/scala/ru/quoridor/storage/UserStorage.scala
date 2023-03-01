package ru.quoridor.storage

import ru.quoridor.model.User
import ru.quoridor.storage.quillInst.{QuillContext, UserStorageImpl}
import ru.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait UserStorage {
  def findByLogin(login: String): Task[User]

  def findById(id: ID[User]): Task[User]

  def insert(user: User): Task[Unit]
}

object UserStorage {
  val live: RLayer[QuillContext, UserStorage] =
    ZLayer.fromFunction(new UserStorageImpl(_))
}
