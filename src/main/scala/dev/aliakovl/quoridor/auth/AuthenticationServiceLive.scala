package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.*
import dev.aliakovl.quoridor.auth.store.RefreshTokenStore
import dev.aliakovl.quoridor.services.UserService
import zio.{IO, Task, URLayer, ZLayer}

class AuthenticationServiceLive(
    userService: UserService,
    accessService: AccessService,
    hashingService: HashingService,
    tokenStore: RefreshTokenStore
) extends AuthenticationService:
  override def signIn(
      credentials: Credentials
  ): Task[(AccessToken, RefreshToken)] = for {
    user <- userService.getUserSecret(credentials.username)
    _ <- hashingService.verifyPassword(credentials.password, user.userSecret)
    accessToken <- accessService.generateToken(
      ClaimData(user.id, user.username)
    )
    refreshToken <- RefreshToken.generate
    _ <- tokenStore.add(refreshToken, user.id)
  } yield (accessToken, refreshToken)

  override def refresh(
      refreshToken: RefreshToken
  ): Task[(AccessToken, RefreshToken)] = for {
    userId <- tokenStore.remove(refreshToken)
    user <- userService.getUser(userId)
    refreshToken <- RefreshToken.generate
    accessToken <- accessService.generateToken(
      ClaimData(user.id, user.username)
    )
    _ <- tokenStore.add(refreshToken, user.id)
  } yield (accessToken, refreshToken)

  override def signOut(
      refreshToken: RefreshToken
  ): IO[AuthException, Unit] = for {
    _ <- tokenStore.remove(refreshToken)
  } yield ()

object AuthenticationServiceLive:
  val live: URLayer[
    UserService & AccessService & HashingService & RefreshTokenStore,
    AuthenticationService
  ] =
    ZLayer.fromFunction(new AuthenticationServiceLive(_, _, _, _))
