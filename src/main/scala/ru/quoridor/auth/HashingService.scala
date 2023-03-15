package ru.quoridor.auth

import ru.quoridor.auth.model.{Password, UserSecret}
import ru.utils.ZIOExtensions.OrFail
import zio.System.env
import zio.{IO, UIO, ULayer, ZIO, ZLayer}

trait HashingService[P, S] {
  def hashPassword(password: P): UIO[S]
  def verifyPassword(password: P, secret: S): IO[Throwable, Unit]
}

object HashingService {
  val live: ULayer[HashingService[Password, UserSecret]] = ZLayer(
    for {
      pepper <- env("PSWD_PEPPER").orDie
    } yield new HashingServiceImpl(pepper.orNull)
  )
}

class HashingServiceImpl(pepper: String)
    extends HashingService[Password, UserSecret] {
  override def hashPassword(password: Password): UIO[UserSecret] = ZIO
    .succeed {
      com.password4j.Password
        .hash(password.value)
        .addRandomSalt()
        .addPepper(pepper)
        .withArgon2()
        .getResultAsBytes
    }
    .map(UserSecret)

  override def verifyPassword(
      password: Password,
      secret: UserSecret
  ): IO[Throwable, Unit] = {
    ZIO
      .succeed(
        com.password4j.Password
          .check(password.value.getBytes, secret.value)
          .addPepper(pepper)
          .withArgon2()
      )
      .orFail(new Throwable("Invalid password"))
  }
}
