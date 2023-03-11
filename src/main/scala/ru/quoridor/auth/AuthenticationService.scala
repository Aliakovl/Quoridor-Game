package ru.quoridor.auth

import ru.quoridor.auth.model._
import ru.quoridor.auth.store.KSetStore
import ru.quoridor.model.User
import ru.quoridor.services.UserService
import ru.utils.tagging.ID
import zio.{Task, ZLayer}

case class Credentials(username: Username, password: Password)

trait AuthenticationService {
  def login(credentials: Credentials): Task[(AccessToken, RefreshToken)]

  def refresh(
      accessToken: AccessToken,
      refreshToken: RefreshToken
  ): Task[(AccessToken, RefreshToken)]

  def logout(accessToken: AccessToken, refreshToken: RefreshToken): Task[Unit]
}

object AuthenticationService {
  val live: ZLayer[UserService with AccessService with HashingService[
    Password,
    UserSecret
  ] with AuthorizationService with KSetStore[
    ID[User],
    RefreshToken
  ], Nothing, AuthenticationServiceImpl] =
    ZLayer.fromFunction(new AuthenticationServiceImpl(_, _, _, _, _))
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
    user <- userService.findUser(credentials.username.value)
    _ <- hashingService
      .verifyPassword(credentials.password, secret = ???)
      .filterOrFail(identity)(new Throwable("Invalid password"))
    refreshToken = RefreshToken.generate()
    _ <- tokenStore.sadd(user.id, refreshToken)
    accessToken <- accessService.generateToken(
      ClaimData(user.id, Username(user.login))
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
