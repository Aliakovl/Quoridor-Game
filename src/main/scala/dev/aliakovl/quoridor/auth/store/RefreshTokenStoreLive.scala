package dev.aliakovl.quoridor.auth.store

import dev.aliakovl.quoridor.auth.model.AuthException.InvalidRefreshToken
import dev.aliakovl.quoridor.auth.model.{InvalidRefreshToken, RefreshToken}
import dev.aliakovl.quoridor.codec.redis.given
import dev.aliakovl.quoridor.config.Configuration
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.ZIOExtensions.*
import dev.aliakovl.utils.redis.RedisStore
import dev.aliakovl.utils.redis.config.TokenStore
import dev.aliakovl.utils.store.KVStore
import dev.aliakovl.utils.tagging.ID
import zio.{IO, RLayer, TaskLayer, ZLayer}

class RefreshTokenStoreLive(
    store: KVStore[RefreshToken, ID[User]]
) extends RefreshTokenStore:
  def add(
      refreshToken: RefreshToken,
      userId: ID[User]
  ): IO[InvalidRefreshToken, Unit] =
    store.set(refreshToken, userId).!.orFail(InvalidRefreshToken)

  def remove(
      refreshToken: RefreshToken
  ): IO[InvalidRefreshToken, ID[User]] =
    store.getDel(refreshToken).!.someOrFail(InvalidRefreshToken)

object RefreshTokenStoreLive:
  val live: RLayer[TokenStore, RefreshTokenStore] =
    ZLayer.makeSome[TokenStore, RefreshTokenStore](
      RedisStore.live[RefreshToken, ID[User]],
      ZLayer.fromFunction(new RefreshTokenStoreLive(_))
    )

  val configuredLive: TaskLayer[RefreshTokenStore] =
    Configuration.tokenStore >>> live
