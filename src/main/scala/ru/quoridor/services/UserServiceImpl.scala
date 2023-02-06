package ru.quoridor.services

import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.storage.{GameStorage, UserStorage}
import zio.{Task, ZIO}

import java.util.UUID

class UserServiceImpl(userStorage: UserStorage, gameStorage: GameStorage)
    extends UserService {

  override def findUser(login: String): Task[User] = {
    userStorage.findByLogin(login)
  }

  override def createUser(login: String): Task[User] = {
    userStorage.insert(login)
  }

  override def usersHistory(userId: UUID): Task[List[GamePreView]] = {
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
