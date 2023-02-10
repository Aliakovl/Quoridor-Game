package ru.quoridor.services

import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.storage.{GameStorage, UserStorage}
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.Implicits.TaggedOps
import zio.{Task, ZIO}

import java.util.UUID

class UserServiceImpl(userStorage: UserStorage, gameStorage: GameStorage)
    extends UserService {

  override def findUser(login: String): Task[User] = {
    userStorage.findByLogin(login)
  }

  override def createUser(login: String): Task[User] = {
    val userId = UUID.randomUUID().tag[User]
    val user = User(userId, login)
    userStorage.insert(user).as(user)
  }

  override def usersHistory(userId: ID[User]): Task[List[GamePreView]] = {
    for {
      gameIds <- userStorage.history(userId)
      gamePreViews <- ZIO.foreachPar(gameIds)(gameStorage.findParticipants)
    } yield gamePreViews
  }
}
