package ru.quoridor.services

import ru.quoridor.storage.{GameStorage, UserStorage}
import ru.quoridor.model.{GamePreView, User}
import zio.{RIO, Task, ZIO, ZLayer}

import java.util.UUID

trait UserService {
  def findUser(login: String): Task[User]

  def createUser(login: String): Task[User]

  def usersHistory(userId: UUID): Task[List[GamePreView]]
}

object UserService {
  val live: ZLayer[UserStorage with GameStorage, Nothing, UserServiceImpl] =
    ZLayer.fromFunction(UserServiceImpl.apply _)

  def findUser(login: String): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.findUser(login))

  def createUser(login: String): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.createUser(login))

  def usersHistory(userId: UUID): RIO[UserService, List[GamePreView]] =
    ZIO.serviceWithZIO[UserService](_.usersHistory(userId))
}
