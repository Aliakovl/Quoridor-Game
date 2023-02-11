package ru.quoridor.services

import ru.quoridor.model.User
import ru.quoridor.storage.UserStorage
import ru.utils.tagging.Tagged.Implicits.TaggedOps
import zio.Task

import java.util.UUID

class UserServiceImpl(userStorage: UserStorage) extends UserService {

  override def findUser(login: String): Task[User] = {
    userStorage.findByLogin(login)
  }

  override def createUser(login: String): Task[User] = {
    val userId = UUID.randomUUID().tag[User]
    val user = User(userId, login)
    userStorage.insert(user).as(user)
  }
}
