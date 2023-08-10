package ru.quoridor.auth.store

import ru.quoridor.auth.model.AuthException.InvalidRefreshToken
import ru.quoridor.auth.model._
import ru.quoridor.model.User
import ru.utils.ZIOExtensions.OrFail
import ru.utils.tagging.Id
import zio._

trait RefreshTokenStore {
  def add(
      refreshToken: RefreshToken,
      userId: Id[User]
  ): IO[InvalidRefreshToken, Unit]

  def remove(
      refreshToken: RefreshToken
  ): IO[InvalidRefreshToken, Id[User]]
}

class RefreshTokenStoreImpl(store: KVStore[RefreshToken, Id[User]])
    extends RefreshTokenStore {
  def add(
      refreshToken: RefreshToken,
      userId: Id[User]
  ): IO[InvalidRefreshToken, Unit] =
    store.set(refreshToken, userId).!.orFail(InvalidRefreshToken)

  def remove(
      refreshToken: RefreshToken
  ): IO[InvalidRefreshToken, Id[User]] =
    store.getDel(refreshToken).!.someOrFail(InvalidRefreshToken)
}

object RefreshTokenStore {
  val live: RLayer[KVStore[RefreshToken, Id[User]], RefreshTokenStore] =
    ZLayer.fromFunction(new RefreshTokenStoreImpl(_))
}
