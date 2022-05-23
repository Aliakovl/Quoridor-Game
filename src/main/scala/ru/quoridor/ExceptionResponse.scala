package ru.quoridor

import GameException._

sealed abstract class ExceptionResponse(errorMessage: String) extends Product with Serializable {
  def message: String = errorMessage
}

object ExceptionResponse {
  def apply(throwable: Throwable): ExceptionResponse = throwable match {
    case ex: GameMoveException => ExceptionResponse400(ex.getMessage)
    case ex: UserNotFoundException => ExceptionResponse404(ex.getMessage)
    case ex: LoginNotFoundException => ExceptionResponse404(ex.getMessage)
    case ex: GameNotFoundException => ExceptionResponse404(ex.getMessage)
    case ex: GameInterloperException => ExceptionResponse403(ex.getMessage)
    case ex: NotGameCreator => ExceptionResponse403(ex.getMessage)
    case ex: GameException => ExceptionResponse400(ex.getMessage)
    case _ => ExceptionResponse500("Something went wrong")
  }

  case class ExceptionResponse400(errorMessage: String)
    extends ExceptionResponse(errorMessage)
  case class ExceptionResponse401(errorMessage: String)
    extends ExceptionResponse(errorMessage)
  case class ExceptionResponse403(errorMessage: String)
    extends ExceptionResponse(errorMessage)
  case class ExceptionResponse404(errorMessage: String)
    extends ExceptionResponse(errorMessage)
  case class ExceptionResponse500(errorMessage: String)
    extends ExceptionResponse(errorMessage)
}