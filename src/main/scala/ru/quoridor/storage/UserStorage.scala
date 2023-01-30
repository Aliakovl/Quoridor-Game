package ru.quoridor.storage

import cats.effect.Resource
import doobie.Transactor
import ru.quoridor.User
import ru.quoridor.game.Game
import ru.quoridor.storage.sqlStorage.UserStorageImpl
import ru.utils.Typed.ID
import zio.{Task, ZLayer}

trait UserStorage {
  def findByLogin(login: String): Task[User]

  def find(id: ID[User]): Task[User]

  def insert(login: String): Task[User]

  def history(id: ID[User]): Task[List[ID[Game]]]
}

object UserStorage {
  val live: ZLayer[
    Resource[Task, Transactor[Task]],
    Nothing,
    UserStorage
  ] = ZLayer.fromFunction(UserStorageImpl.apply _)
}
