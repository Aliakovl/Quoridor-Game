package dev.aliakovl.quoridor.auth.store

import dev.aliakovl.quoridor.auth.model.*
import dev.aliakovl.quoridor.model.User
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
