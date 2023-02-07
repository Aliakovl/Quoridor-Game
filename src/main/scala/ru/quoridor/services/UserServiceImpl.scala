package ru.quoridor.services

import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.storage.{GameStorage, UserStorage}
import ru.utils.Tagged.ID
import zio.{Task, ZIO}

class UserServiceImpl(userStorage: UserStorage, gameStorage: GameStorage)
    extends UserService {

  override def findUser(login: String): Task[User] = {
    userStorage.findByLogin(login)
  }

  override def createUser(login: String): Task[User] = {
    userStorage.insert(login)
  }

  override def usersHistory(userId: ID[User]): Task[List[GamePreView]] = {
    for {
      gameIds <- userStorage.history(userId)
      gamePreViews <-
        if (gameIds.isEmpty) { ZIO.succeed(List.empty[GamePreView]) }
        else {
          ZIO.foreachPar(gameIds)(gameStorage.findParticipants)
        }
    } yield gamePreViews
  }
}

object UserServiceImpl {
  def apply(
      userStorage: UserStorage,
      gameStorage: GameStorage
  ): UserServiceImpl = new UserServiceImpl(userStorage, gameStorage)
}
