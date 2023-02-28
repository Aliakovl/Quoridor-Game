package ru.quoridor.storage

import io.getquill.{CompositeNamingStrategy2, Escape, SnakeCase}
import io.getquill.jdbczio.Quill
import ru.quoridor.model.User
import ru.quoridor.storage.quillInst.UserStorageImpl
import ru.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait UserStorage {
  def findByLogin(login: String): Task[User]

  def findById(id: ID[User]): Task[User]

  def insert(user: User): Task[Unit]
}

object UserStorage {
  val live: RLayer[Quill.Postgres[
    CompositeNamingStrategy2[SnakeCase, Escape]
  ], UserStorage] =
    ZLayer.fromFunction(new UserStorageImpl(_))
}
