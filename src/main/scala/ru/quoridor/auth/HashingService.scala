package ru.quoridor.auth

import ru.quoridor.auth.model.AuthException.InvalidPassword
import ru.quoridor.auth.model.{InvalidPassword, Password, UserSecret}
import ru.utils.ZIOExtensions.*
import zio.System.env
import zio.*

trait HashingService[P, S]:
  def hashPassword(password: P): UIO[S]
  def verifyPassword(password: P, secret: S): IO[InvalidPassword, Unit]

object HashingService:
  val live: ULayer[HashingService[Password, UserSecret]] = ZLayer(
    for {
      pepper <- env("PSWD_PEPPER").!
    } yield new HashingServiceImpl(pepper.orNull)
  )

class HashingServiceImpl(pepper: String)
    extends HashingService[Password, UserSecret]:
  override def hashPassword(password: Password): UIO[UserSecret] = ZIO
    .succeed {
      com.password4j.Password
        .hash(password.value)
        .addRandomSalt()
        .addPepper(pepper)
        .withArgon2()
        .getResultAsBytes
    }
    .map(UserSecret(_))

  override def verifyPassword(
      password: Password,
      secret: UserSecret
  ): IO[InvalidPassword, Unit] = {
    ZIO
      .succeed(
        com.password4j.Password
          .check(password.value.getBytes, secret.value)
          .addPepper(pepper)
          .withArgon2()
      )
      .orFail(InvalidPassword)
  }
