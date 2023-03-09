package ru.quoridor.auth

import ru.quoridor.auth.model.{AccessToken, Password, RefreshToken, Username}
import ru.quoridor.auth.store.KVStore
import zio.{RLayer, Task, ZLayer}

case class Credentials(username: Username, password: Password)

trait AuthorizationService {
  def login(credentials: Credentials): Task[(AccessToken, RefreshToken)]

  def refresh(
      username: Username,
      refreshToken: RefreshToken
  ): Task[(AccessToken, RefreshToken)]

  def logout(username: Username, refreshToken: RefreshToken): Task[Unit]
}

object AuthorizationService {
  val live: RLayer[KVStore[RefreshToken, Username], AuthorizationServiceImpl] =
    ZLayer.fromFunction(new AuthorizationServiceImpl(_))
}

class AuthorizationServiceImpl(tokenStore: KVStore[RefreshToken, Username])
    extends AuthorizationService {
  override def login(
      credentials: Credentials
  ): Task[(AccessToken, RefreshToken)] = ???

  override def refresh(
      username: Username,
      refreshToken: RefreshToken
  ): Task[(AccessToken, RefreshToken)] = ???

  override def logout(
      username: Username,
      refreshToken: RefreshToken
  ): Task[Unit] = ???
}
