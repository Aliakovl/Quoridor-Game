package dev.aliakovl.quoridor.auth.store

import dev.aliakovl.quoridor.auth.model.AuthException.InvalidRefreshToken
import dev.aliakovl.quoridor.auth.model.*
import dev.aliakovl.quoridor.auth.store.redis.RedisStore
import dev.aliakovl.quoridor.codec.redis.given
import dev.aliakovl.quoridor.config.TokenStore
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.ZIOExtensions.*
import dev.aliakovl.utils.tagging.ID
import zio.*

trait RefreshTokenStore:
  def add(
      refreshToken: RefreshToken,
      userId: ID[User]
  ): IO[InvalidRefreshToken, Unit]

  def remove(
      refreshToken: RefreshToken
  ): IO[InvalidRefreshToken, ID[User]]

class RefreshTokenStoreImpl(store: KVStore[RefreshToken, ID[User]])
    extends RefreshTokenStore:
  def add(
      refreshToken: RefreshToken,
      userId: ID[User]
  ): IO[InvalidRefreshToken, Unit] =
    store.set(refreshToken, userId).!.orFail(InvalidRefreshToken)

  def remove(
      refreshToken: RefreshToken
  ): IO[InvalidRefreshToken, ID[User]] =
    store.getDel(refreshToken).!.someOrFail(InvalidRefreshToken)

object RefreshTokenStore:
  val live: RLayer[TokenStore, RefreshTokenStore] =
    ZLayer.makeSome[TokenStore, RefreshTokenStore](
      RedisStore.live[RefreshToken, ID[User]],
      ZLayer.fromFunction(new RefreshTokenStoreImpl(_))
    )
