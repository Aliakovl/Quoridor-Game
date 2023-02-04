package ru.quoridor.storage

import ru.quoridor.User
import ru.quoridor.game.Game
import ru.quoridor.storage.sqlStorage.UserStorageImpl
import ru.utils.Typed.ID
import zio.{RLayer, Task, ZLayer}

trait UserStorage {
  def findByLogin(login: String): Task[User]

  def find(id: ID[User]): Task[User]

  def insert(login: String): Task[User]

  def history(id: ID[User]): Task[List[ID[Game]]]
}

object UserStorage {
  val live: RLayer[DataBase, UserStorage] =
    ZLayer.fromFunction(UserStorageImpl.apply _)
}
