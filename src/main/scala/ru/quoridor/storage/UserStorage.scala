package ru.quoridor.storage

import ru.quoridor.User
import ru.quoridor.game.Game
import ru.utils.Typed.ID


trait UserStorage[F[_]] {
  def findByLogin(login: String): F[User]

  def find(id: ID[User]): F[User]

  def insert(login: String): F[User]

  def history(id: ID[User]): F[List[ID[Game]]]
}
