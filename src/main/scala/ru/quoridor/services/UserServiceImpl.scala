package ru.quoridor.services

import ru.quoridor.auth.HashingService
import ru.quoridor.auth.model.{Credentials, Password, UserSecret, Username}
import ru.quoridor.model.{User, UserWithSecret}
import ru.quoridor.dao.UserDao
import ru.quoridor.model.User.Userdata
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.Implicits._
import zio.Task

import java.util.UUID

class UserServiceImpl(
    userDao: UserDao,
    hashingService: HashingService[Password, UserSecret]
) extends UserService {
  override def getUser(userId: ID[User]): Task[Userdata] = {
    userDao.findById(userId)
  }

  def getUser(username: Username): Task[Userdata] = {
    userDao.findByUsername(username).map {
      case UserWithSecret(id, username, _) =>
        User.Userdata(id, username)
    }
  }

  override def createUser(credentials: Credentials): Task[Userdata] = {
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
