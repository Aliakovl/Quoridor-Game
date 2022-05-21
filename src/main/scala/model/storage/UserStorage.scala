package model.storage

import model.User
import model.game.Game
import utils.Typed.ID


trait UserStorage[F[_]] {
  def findByLogin(login: String): F[User]

  def find(id: ID[User]): F[User]

  def insert(login: String): F[User]

  def history(id: ID[User]): F[List[ID[Game]]]
}
