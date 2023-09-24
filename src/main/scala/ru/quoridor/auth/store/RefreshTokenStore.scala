package ru.quoridor.auth.store

import ru.quoridor.auth.model.AuthException.InvalidRefreshToken
import ru.quoridor.auth.model.*
import ru.quoridor.model.User
import ru.utils.ZIOExtensions.*
import ru.utils.tagging.ID
import zio.*

trait RefreshTokenStore {
  def add(
      refreshToken: RefreshToken,
      userId: ID[User]
  ): IO[InvalidRefreshToken, Unit]

  def remove(
      refreshToken: RefreshToken
  ): IO[InvalidRefreshToken, ID[User]]
}

class RefreshTokenStoreImpl(store: KVStore[RefreshToken, ID[User]])
    extends RefreshTokenStore {
  def add(
      refreshToken: RefreshToken,
      userId: ID[User]
  ): IO[InvalidRefreshToken, Unit] =
    store.set(refreshToken, userId).!.orFail(InvalidRefreshToken)

  def remove(
      refreshToken: RefreshToken
  ): IO[InvalidRefreshToken, ID[User]] =
    store.getDel(refreshToken).!.someOrFail(InvalidRefreshToken)
}

object RefreshTokenStore {
  val live: RLayer[KVStore[RefreshToken, ID[User]], RefreshTokenStore] =
    ZLayer.fromFunction(new RefreshTokenStoreImpl(_))
}
