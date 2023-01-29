package ru.quoridor.services

import ru.quoridor.{GamePreView, User}
import ru.utils.Typed.ID
import zio.Task

trait UserService {
  def findUser(login: String): Task[User]

  def createUser(login: String): Task[User]

  def usersHistory(userId: ID[User]): Task[List[GamePreView]]
}
