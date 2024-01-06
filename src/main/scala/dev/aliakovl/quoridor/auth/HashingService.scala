package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.{InvalidPassword, Password, UserSecret}
import zio.*

trait HashingService:
  def hashPassword(password: Password): UIO[UserSecret]

  def verifyPassword(
      password: Password,
      secret: UserSecret
  ): IO[InvalidPassword, Unit]
