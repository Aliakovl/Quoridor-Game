package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.*
import zio.*

trait AuthenticationService:
  def signIn(credentials: Credentials): Task[(AccessToken, RefreshToken)]

  def refresh(
      refreshToken: RefreshToken
  ): Task[(AccessToken, RefreshToken)]

  def signOut(
      refreshToken: RefreshToken
  ): IO[AuthException, Unit]
