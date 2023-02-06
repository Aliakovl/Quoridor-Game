package ru.quoridor.services

import ru.quoridor.storage.{GameStorage, UserStorage}
import ru.quoridor.model.{GamePreView, User}
import ru.utils.Typed.ID
import zio.{RIO, Task, ZIO, ZLayer}

trait UserService {
  def findUser(login: String): Task[User]

  def createUser(login: String): Task[User]

  def usersHistory(userId: ID[User]): Task[List[GamePreView]]
}

object UserService {
  val live: ZLayer[UserStorage with GameStorage, Nothing, UserServiceImpl] =
    ZLayer.fromFunction(UserServiceImpl.apply _)

  def findUser(login: String): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.findUser(login))

  def createUser(login: String): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.createUser(login))

  def usersHistory(userId: ID[User]): RIO[UserService, List[GamePreView]] =
    ZIO.serviceWithZIO[UserService](_.usersHistory(userId))
}
