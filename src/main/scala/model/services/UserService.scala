package model.services

import model.User


trait UserService[F[_]] {
  def createUser(login: String): F[User]
}
