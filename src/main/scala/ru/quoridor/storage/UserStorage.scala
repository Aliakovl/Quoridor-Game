package ru.quoridor.storage

import ru.quoridor.model.User
import ru.quoridor.model.game.Game
import ru.quoridor.storage.sqlStorage.UserStorageImpl
import ru.utils.Typed.ID
import zio.{RLayer, Task, ZLayer}

trait UserStorage {
  def findByLogin(login: String): Task[User]

  def find(userId: ID[User]): Task[User]

  def insert(login: String): Task[User]

  def history(userId: ID[User]): Task[List[ID[Game]]]
}

object UserStorage {
  val live: RLayer[DataBase, UserStorage] =
    ZLayer.fromFunction(UserStorageImpl.apply _)
}
