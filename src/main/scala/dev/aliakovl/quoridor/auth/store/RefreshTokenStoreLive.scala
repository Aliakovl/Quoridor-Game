package dev.aliakovl.quoridor.auth.store

import dev.aliakovl.quoridor.auth.model.AuthException.InvalidRefreshToken
import dev.aliakovl.quoridor.auth.model.{InvalidRefreshToken, RefreshToken}
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.ZIOExtensions.*
import dev.aliakovl.utils.tagging.ID
import zio.IO

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
