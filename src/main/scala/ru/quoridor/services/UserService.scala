package ru.quoridor.services

import ru.quoridor.storage.{GameStorage, UserStorage}
import ru.quoridor.{GamePreView, User}
import ru.utils.Typed.ID
import zio.{Task, ZLayer}

trait UserService {
  def findUser(login: String): Task[User]

  def createUser(login: String): Task[User]

  def usersHistory(userId: ID[User]): Task[List[GamePreView]]
}

object UserService {
  val live: ZLayer[UserStorage with GameStorage, Nothing, UserServiceImpl] =
    ZLayer.fromFunction(UserServiceImpl.apply _)
}
