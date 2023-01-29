package ru.quoridor.storage

import ru.quoridor.User
import ru.quoridor.game.Game
import ru.utils.Typed.ID
import zio.Task

trait UserStorage {
  def findByLogin(login: String): Task[User]

  def find(id: ID[User]): Task[User]

  def insert(login: String): Task[User]

  def history(id: ID[User]): Task[List[ID[Game]]]
}
