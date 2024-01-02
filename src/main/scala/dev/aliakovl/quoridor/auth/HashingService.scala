package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.{InvalidPassword, Password, UserSecret}
import zio.System.env
import zio.*

trait HashingService[P, S]:
  def hashPassword(password: P): UIO[S]

  def verifyPassword(password: P, secret: S): IO[InvalidPassword, Unit]

object HashingService:
  val live: ULayer[HashingService[Password, UserSecret]] = ZLayer(
    for {
      pepper <- env("PSWD_PEPPER").!
    } yield new HashingServiceLive(pepper.orNull)
  )
