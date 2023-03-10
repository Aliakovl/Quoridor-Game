package ru.quoridor.auth.model

import java.util.UUID

case class RefreshToken(value: UUID) extends AnyVal

object RefreshToken {
  def generate(): RefreshToken =
    RefreshToken(UUID.randomUUID())
}
