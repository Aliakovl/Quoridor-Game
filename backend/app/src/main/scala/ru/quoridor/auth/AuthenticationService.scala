package ru.quoridor.auth

import ru.quoridor.auth.model._
import ru.quoridor.auth.store.RefreshTokenStore
import ru.quoridor.services.UserService
import zio._

trait AuthenticationService {
  def signIn(credentials: Credentials): Task[(AccessToken, RefreshToken)]

  def refresh(
      refreshToken: RefreshToken
  ): Task[(AccessToken, RefreshToken)]

  def signOut(
      refreshToken: RefreshToken
  ): IO[AuthException, Unit]
}

object AuthenticationService {
  val live: RLayer[UserService with AccessService with HashingService[
    Password,
    UserSecret
  ] with RefreshTokenStore, AuthenticationService] =
    ZLayer.fromFunction(new AuthenticationServiceImpl(_, _, _, _))

  def signIn(
      credentials: Credentials
  ): RIO[AuthenticationService, (AccessToken, RefreshToken)] =
    ZIO.serviceWithZIO[AuthenticationService](_.signIn(credentials))

  def refresh(
      refreshToken: RefreshToken
  ): RIO[AuthenticationService, (AccessToken, RefreshToken)] =
    ZIO.serviceWithZIO[AuthenticationService](
      _.refresh(refreshToken)
    )

  def signOut(
      refreshToken: RefreshToken
  ): RIO[AuthenticationService, Unit] =
    ZIO.serviceWithZIO[AuthenticationService](
      _.signOut(refreshToken)
    )
}

class AuthenticationServiceImpl(
    userService: UserService,
    accessService: AccessService,
    hashingService: HashingService[Password, UserSecret],
    tokenStore: RefreshTokenStore
) extends AuthenticationService {
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
}
