package model.services

import model.{GamePreView, User}
import utils.Typed.ID


trait UserService[F[_]] {
  def createUser(login: String): F[User]

  def usersHistory(userId: ID[User]): F[List[GamePreView]]
}
