package ru.quoridor.auth.store

import ru.quoridor.auth.model.AuthException.InvalidRefreshToken
import ru.quoridor.auth.model._
import ru.utils.ZIOExtensions.OrFail
import zio._

trait RefreshTokenStore {
  def add(
      refreshToken: RefreshToken,
      tokenSignature: TokenSignature
  ): IO[InvalidRefreshToken, Unit]

  def remove(
      refreshToken: RefreshToken,
      tokenSignature: TokenSignature
  ): IO[InvalidRefreshToken, Unit]
}

class RefreshTokenStoreImpl(store: KVStore[RefreshToken, TokenSignature])
    extends RefreshTokenStore {
  def add(
      refreshToken: RefreshToken,
      tokenSignature: TokenSignature
  ): IO[InvalidRefreshToken, Unit] =
    store.set(refreshToken, tokenSignature).!.orFail(InvalidRefreshToken)

  def remove(
      refreshToken: RefreshToken,
      tokenSignature: TokenSignature
  ): IO[InvalidRefreshToken, Unit] =
    store.delete(refreshToken, tokenSignature).!.orFail(InvalidRefreshToken)
}

object RefreshTokenStore {
  val live: RLayer[KVStore[RefreshToken, TokenSignature], RefreshTokenStore] =
    ZLayer.fromFunction(new RefreshTokenStoreImpl(_))
}
