package ru.quoridor.auth

import ru.quoridor.auth.model.{Password, UserSecret}
import zio.System.env
import zio.{UIO, ULayer, ZIO, ZLayer}

trait HashingService[P, S] {
  def hashPassword(password: P): UIO[S]
  def verifyPassword(password: P, secret: S): UIO[Boolean]
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
    }
    .tap(x => ZIO.logInfo(x.toString))
    .map(_.getResultAsBytes)
    .map(UserSecret)

  override def verifyPassword(
      password: Password,
      secret: UserSecret
  ): UIO[Boolean] = {
    ZIO.succeed(secret).map { case UserSecret(value) =>
      com.password4j.Password
        .check(password.value.getBytes, value)
        .addPepper(pepper)
        .withArgon2()
    }
  }
}
