package model.services

import cats.effect.Async
import cats.implicits._
import model.{GamePreView, User}
import model.storage.{GameStorage, UserStorage}
import utils.Typed.ID


class UserServiceImpl[F[_]](userStorage: UserStorage[F],
                            gameStorage: GameStorage[F])
                           (implicit F: Async[F]) extends UserService[F] {

  override def findUser(login: String): F[User] = {
    userStorage.findByLogin(login)
  }

  override def createUser(login: String): F[User] = {
    userStorage.insert(login)
  }

  override def usersHistory(userId: ID[User]): F[List[GamePreView]] = {
    for {
      gameIds <- userStorage.history(userId)
      gamePreViews <- if (gameIds.isEmpty) {F.pure(List.empty[GamePreView])} else {
        F.parSequenceN(gameIds.size){
          gameIds.map{ gameId =>
            gameStorage.findParticipants(gameId)
          }
        }
      }
    } yield gamePreViews
  }
}
