package ru.quoridor.auth.store

import ru.quoridor.auth.model.AuthException.InvalidRefreshToken
import ru.quoridor.auth.model.{InvalidRefreshToken, RefreshToken}
import ru.quoridor.model.User
import ru.utils.ZIOExtensions.OrFail
import ru.utils.tagging.ID
import zio._

trait RefreshTokenStore {
  def add(
      userId: ID[User],
      token: RefreshToken
  ): IO[InvalidRefreshToken, Unit]

  def remove(
      userId: ID[User],
      token: RefreshToken
  ): IO[InvalidRefreshToken, Unit]
}

class RefreshTokenStoreImpl(store: KVStore[RefreshToken, ID[User]])
    extends RefreshTokenStore {
  def add(
      userId: ID[User],
      token: RefreshToken
  ): IO[InvalidRefreshToken, Unit] = {
    store
      .set(token, userId)
      .orDie
      .orFail(InvalidRefreshToken)
  }

  def remove(
      userId: ID[User],
      token: RefreshToken
  ): IO[InvalidRefreshToken, Unit] = {
    store
      .delete(token, userId)
      .orDie
      .orFail(InvalidRefreshToken)
  }
}

object RefreshTokenStore {
  val live: RLayer[KVStore[RefreshToken, ID[User]], RefreshTokenStore] =
    ZLayer.fromFunction(new RefreshTokenStoreImpl(_))
}
