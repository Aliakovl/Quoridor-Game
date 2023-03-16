package ru.quoridor.auth

import ru.quoridor.auth.model._
import ru.quoridor.auth.store.RefreshTokenStore
import ru.quoridor.services.UserService
import zio._

trait AuthenticationService {
  def signIn(credentials: Credentials): Task[(AccessToken, RefreshToken)]

  def refresh(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): IO[AuthException, (AccessToken, RefreshToken)]

  def signOut(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): IO[AuthException, Unit]
}

object AuthenticationService {
  val live: RLayer[UserService with AccessService with HashingService[
    Password,
    UserSecret
  ] with AuthorizationService with RefreshTokenStore, AuthenticationService] =
    ZLayer.fromFunction(new AuthenticationServiceImpl(_, _, _, _, _))

  def signIn(
      credentials: Credentials
  ): RIO[AuthenticationService, (AccessToken, RefreshToken)] =
    ZIO.serviceWithZIO[AuthenticationService](_.signIn(credentials))

  def refresh(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): RIO[AuthenticationService, (AccessToken, RefreshToken)] =
    ZIO.serviceWithZIO[AuthenticationService](
      _.refresh(accessToken, refreshToken)
    )

  def signOut(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): RIO[AuthenticationService, Unit] =
    ZIO.serviceWithZIO[AuthenticationService](
      _.signOut(accessToken, refreshToken)
    )
}

class AuthenticationServiceImpl(
    userService: UserService,
    accessService: AccessService,
    hashingService: HashingService[Password, UserSecret],
    authorizationService: AuthorizationService,
    tokenStore: RefreshTokenStore
) extends AuthenticationService {
  override def signIn(
      credentials: Credentials
  ): Task[(AccessToken, RefreshToken)] = for {
    user <- userService.getUserSecret(credentials.username)
    _ <- hashingService.verifyPassword(credentials.password, user.userSecret)
    refreshToken <- RefreshToken.generate
    _ <- tokenStore.add(user.id, refreshToken)
    accessToken <- accessService.generateToken(
      ClaimData(user.id, user.username)
    )
  } yield (accessToken, refreshToken)

  override def refresh(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): IO[AuthException, (AccessToken, RefreshToken)] = for {
    cd <- authorizationService.verifySign(accessToken)
    _ <- tokenStore.remove(cd.userId, refreshToken)
    refreshToken <- RefreshToken.generate
    _ <- tokenStore.add(cd.userId, refreshToken)
    accessToken <- accessService.generateToken(cd)
  } yield (accessToken, refreshToken)

  override def signOut(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): IO[AuthException, Unit] = for {
    cd <- authorizationService.validate(accessToken)
    _ <- tokenStore.remove(cd.userId, refreshToken)
  } yield ()
}
