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
    accessToken <- accessService.generateToken(
      ClaimData(user.id, user.username)
    )
    refreshToken <- RefreshToken.generate
    signature <- authorizationService.extractSignature(accessToken)
    _ <- tokenStore.add(refreshToken, signature)
  } yield (accessToken, refreshToken)

  override def refresh(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): IO[AuthException, (AccessToken, RefreshToken)] = for {
    claimData <- authorizationService.verifySign(accessToken)
    signature <- authorizationService.extractSignature(accessToken)
    _ <- tokenStore.remove(refreshToken, signature)
    refreshToken <- RefreshToken.generate
    accessToken <- accessService.generateToken(claimData)
    signature <- authorizationService.extractSignature(accessToken)
    _ <- tokenStore.add(refreshToken, signature)
  } yield (accessToken, refreshToken)

  override def signOut(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): IO[AuthException, Unit] = for {
    _ <- authorizationService.validate(accessToken)
    signature <- authorizationService.extractSignature(accessToken)
    _ <- tokenStore.remove(refreshToken, signature)
  } yield ()
}
