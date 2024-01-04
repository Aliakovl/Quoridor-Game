package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.GameException
import dev.aliakovl.quoridor.auth.model.AuthException
import dev.aliakovl.quoridor.GameException.*
import dev.aliakovl.quoridor.model.GameMoveException
import sttp.model.StatusCode

final case class ExceptionResponse(errorMessage: String)

object ExceptionResponse:
  def apply(throwable: Throwable): (ExceptionResponse, StatusCode) = {
    exceptionCode(throwable) match {
      case s @ StatusCode.InternalServerError =>
        (ExceptionResponse("Oops, something went wrong..."), s)
      case s => (ExceptionResponse(throwable.getMessage), s)
    }
  }

  private def exceptionCode(throwable: Throwable): StatusCode =
    throwable match {
      case _: AuthException             => StatusCode.Unauthorized
      case _: GameMoveException         => StatusCode.BadRequest
      case _: UserNotFoundException     => StatusCode.NotFound
      case _: UsernameNotFoundException => StatusCode.NotFound
      case _: GameNotFoundException     => StatusCode.NotFound
      case _: GameInterloperException   => StatusCode.Forbidden
      case _: NotGameCreatorException   => StatusCode.Forbidden
      case _: GameException             => StatusCode.BadRequest
      case _                            => StatusCode.InternalServerError
    }
