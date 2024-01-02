package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.*
import dev.aliakovl.quoridor.auth.store.RefreshTokenStore
import dev.aliakovl.quoridor.services.UserService
import zio.*

trait AuthenticationService:
  def signIn(credentials: Credentials): Task[(AccessToken, RefreshToken)]

  def refresh(
      refreshToken: RefreshToken
  ): Task[(AccessToken, RefreshToken)]

  def signOut(
      refreshToken: RefreshToken
  ): IO[AuthException, Unit]

object AuthenticationService:
  val live: URLayer[
    UserService & AccessService & HashingService[Password, UserSecret] &
      RefreshTokenStore,
    AuthenticationService
  ] =
    ZLayer.fromFunction(new AuthenticationServiceLive(_, _, _, _))
