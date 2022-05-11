package model.services

import cats.effect.Async
import model.User
import model.storage.UserStorage


class UserServiceImpl[F[_]](userStorage: UserStorage[F])
                           (implicit F: Async[F]) extends UserService[F] {
  override def createUser(login: String): F[User] = {
    userStorage.insert(login)
  }
}
