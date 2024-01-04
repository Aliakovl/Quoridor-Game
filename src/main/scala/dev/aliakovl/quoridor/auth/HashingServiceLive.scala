package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.AuthException.InvalidPassword
import dev.aliakovl.quoridor.auth.model.{InvalidPassword, Password, UserSecret}
import dev.aliakovl.utils.ZIOExtensions.*
import zio.{IO, UIO, ZIO}

class HashingServiceLive(
    pepper: String
) extends HashingService:
  override def hashPassword(password: Password): UIO[UserSecret] = ZIO
    .succeed {
      com.password4j.Password
        .hash(password.value)
        .addRandomSalt()
        .addPepper(pepper)
        .withArgon2()
        .getResultAsBytes
    }
    .map(UserSecret.apply)

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
