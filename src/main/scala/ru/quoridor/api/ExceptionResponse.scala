package ru.quoridor.api

import ru.quoridor.model.GameException._
import ru.quoridor.model.{GameException, GameMoveException}
import sttp.model.StatusCode

final case class ExceptionResponse(errorMessage: String)

object ExceptionResponse {
  def apply(throwable: Throwable): ExceptionResponse = {
    exceptionCode(throwable) match {
      case StatusCode.InternalServerError =>
        ExceptionResponse("Oops! Something went wrong...")
      case _ => ExceptionResponse(throwable.getMessage)
    }
  }

  def exceptionCode(throwable: Throwable): StatusCode = throwable match {
    case _: GameMoveException       => StatusCode.BadRequest
    case _: UserNotFoundException   => StatusCode.NotFound
    case _: LoginNotFoundException  => StatusCode.NotFound
    case _: GameNotFoundException   => StatusCode.NotFound
    case _: GameInterloperException => StatusCode.Forbidden
    case _: NotGameCreatorException => StatusCode.Forbidden
    case _: GameException           => StatusCode.BadRequest
    case _                          => StatusCode.InternalServerError
  }
}
