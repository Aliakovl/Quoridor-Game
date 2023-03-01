package ru.quoridor.services

import ru.quoridor.model.User
import ru.quoridor.storage.UserDao
import ru.utils.tagging.Tagged.Implicits.TaggedOps
import zio.Task

import java.util.UUID

class UserServiceImpl(userDao: UserDao) extends UserService {

  override def findUser(login: String): Task[User] = {
    userDao.findByLogin(login)
  }

  override def createUser(login: String): Task[User] = {
    val userId = UUID.randomUUID().tag[User]
    val user = User(userId, login)
    userDao.insert(user).as(user)
  }
}
