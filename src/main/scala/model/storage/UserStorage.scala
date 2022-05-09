package model.storage

import model.User
import utils.Typed.ID


trait UserStorage[F[_]] {
  def find(id: ID[User]): F[User]

  def insert(login: String): F[User]
}
