package model.storage

import model.User
import java.util.UUID


trait UserStorage[F[_]] {
  def find(id: UUID): F[User]

  def insert(login: String): F[User]
}
