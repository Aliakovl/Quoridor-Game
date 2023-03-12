package ru.quoridor.auth

import ru.quoridor.auth.model._
import ru.quoridor.auth.store.KSetStore
import ru.quoridor.model.User
import ru.quoridor.services.UserService
import ru.utils.tagging.ID
import zio.{RIO, RLayer, Task, ZIO, ZLayer}

trait AuthenticationService {
  def login(credentials: Credentials): Task[(AccessToken, RefreshToken)]

  def refresh(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): Task[(AccessToken, RefreshToken)]

  def logout(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): Task[Unit]
}

object AuthenticationService {
  val live: RLayer[UserService with AccessService with HashingService[
    Password,
    UserSecret
  ] with AuthorizationService with KSetStore[
    ID[User],
    RefreshToken
  ], AuthenticationServiceImpl] =
    ZLayer.fromFunction(new AuthenticationServiceImpl(_, _, _, _, _))

  def login(
      credentials: Credentials
  ): RIO[AuthenticationService, (AccessToken, RefreshToken)] =
    ZIO.serviceWithZIO[AuthenticationService](_.login(credentials))

  def refresh(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): RIO[AuthenticationService, (AccessToken, RefreshToken)] =
    ZIO.serviceWithZIO[AuthenticationService](
      _.refresh(accessToken, refreshToken)
    )

  def logout(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): RIO[AuthenticationService, Unit] =
    ZIO.serviceWithZIO[AuthenticationService](
      _.logout(accessToken, refreshToken)
    )
}

class AuthenticationServiceImpl(
    userService: UserService,
    accessService: AccessService,
    hashingService: HashingService[Password, UserSecret],
    authorizationService: AuthorizationService,
    tokenStore: KSetStore[ID[User], RefreshToken]
) extends AuthenticationService {
  override def login(
      credentials: Credentials
  ): Task[(AccessToken, RefreshToken)] = for {
    user <- userService.getUserSecret(credentials.username)
    _ <- hashingService
      .verifyPassword(credentials.password, user.userSecret)
      .filterOrFail(identity)(new Throwable("Invalid password"))
    refreshToken = RefreshToken.generate()
    _ <- tokenStore.sadd(user.id, refreshToken)
    accessToken <- accessService.generateToken(
      ClaimData(user.id, user.username)
    )
  } yield (accessToken, refreshToken)

  override def refresh(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): Task[(AccessToken, RefreshToken)] = for {
    cd <- authorizationService.verified(accessToken)
    _ <- tokenStore
      .srem(cd.userId, refreshToken)
      .filterOrFail(_ > 0)(new Throwable("Invalid token"))
    refreshToken = RefreshToken.generate()
    _ <- tokenStore.sadd(cd.userId, refreshToken)
    accessToken <- accessService.generateToken(cd)
  } yield (accessToken, refreshToken)

  override def logout(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): Task[Unit] = for {
    cd <- authorizationService.verified(accessToken)
    _ <- tokenStore
      .srem(cd.userId, refreshToken)
      .filterOrFail(_ > 0)(new Throwable("Invalid token"))
  } yield ()
}
