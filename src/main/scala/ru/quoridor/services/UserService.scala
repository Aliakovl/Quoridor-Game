package ru.quoridor.services

import ru.quoridor.dao.{GameDao, UserDao}
import ru.quoridor.model.User
import zio.{RIO, Task, URLayer, ZIO, ZLayer}

trait UserService {
  def findUser(login: String): Task[User]

  def createUser(login: String): Task[User]
}

object UserService {
  val live: URLayer[UserDao with GameDao, UserService] =
    ZLayer.fromFunction(new UserServiceImpl(_))

  def findUser(login: String): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.findUser(login))

  def createUser(login: String): RIO[UserService, User] =
    ZIO.serviceWithZIO[UserService](_.createUser(login))
}
