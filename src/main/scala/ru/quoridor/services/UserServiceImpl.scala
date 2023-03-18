package ru.quoridor.services

import ru.quoridor.auth.HashingService
import ru.quoridor.auth.model.{Credentials, Password, UserSecret, Username}
import ru.quoridor.model.{User, UserWithSecret}
import ru.quoridor.dao.UserDao
import ru.utils.tagging.Tagged.Implicits._
import zio.Task

import java.util.UUID

class UserServiceImpl(
    userDao: UserDao,
    hashingService: HashingService[Password, UserSecret]
) extends UserService {
  override def findUser(username: Username): Task[User] = {
    userDao.findByUsername(username).map(_.toUser)
  }

  override def createUser(credentials: Credentials): Task[User] = {
    for {
      secret <- hashingService.hashPassword(credentials.password)
      userId = UUID.randomUUID().tag[User]
      user = UserWithSecret(userId, credentials.username, secret)
      _ <- userDao.insert(user)
    } yield user.toUser
  }

  override def getUserSecret(username: Username): Task[UserWithSecret] = {
    userDao.findByUsername(username)
  }
}
