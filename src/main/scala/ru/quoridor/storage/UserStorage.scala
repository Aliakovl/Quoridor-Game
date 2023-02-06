package ru.quoridor.storage

import ru.quoridor.model.User
import ru.quoridor.storage.sqlStorage.UserStorageImpl
import zio.{RLayer, Task, ZLayer}

import java.util.UUID

trait UserStorage {
  def findByLogin(login: String): Task[User]

  def find(userId: UUID): Task[User]

  def insert(login: String): Task[User]

  def history(userId: UUID): Task[List[UUID]]
}

object UserStorage {
  val live: RLayer[DataBase, UserStorage] =
    ZLayer.fromFunction(UserStorageImpl.apply _)
}
