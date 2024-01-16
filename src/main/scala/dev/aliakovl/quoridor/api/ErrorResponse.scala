package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.GameException
import dev.aliakovl.quoridor.GameException.*

sealed trait ErrorResponse:
  def errorMessage: String

final case class Unauthorized(errorMessage: String) extends ErrorResponse
final case class BadRequest(errorMessage: String) extends ErrorResponse
final case class NotFound(errorMessage: String) extends ErrorResponse
final case class Forbidden(errorMessage: String) extends ErrorResponse
final case class Conflict(errorMessage: String) extends ErrorResponse
final case class InternalServerError(errorMessage: String = "Unexpected")
    extends ErrorResponse
