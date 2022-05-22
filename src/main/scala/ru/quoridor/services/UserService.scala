package ru.quoridor.services

import ru.quoridor.{GamePreView, User}
import ru.utils.Typed.ID


trait UserService[F[_]] {
  def findUser(login: String): F[User]

  def createUser(login: String): F[User]

  def usersHistory(userId: ID[User]): F[List[GamePreView]]
}
