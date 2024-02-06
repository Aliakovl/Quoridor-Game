package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.GameException
import dev.aliakovl.quoridor.GameException.*
import dev.aliakovl.quoridor.engine.GameMoveException
import zio.ZIO

object ErrorMapping:
  def defaultErrorsMapping[R, E <: Throwable, A](
      zio: ZIO[R, E, A]
  ): ZIO[R, ErrorResponse, A] = zio.mapError {
    case e: GameMoveException         => Conflict(e.getMessage)
    case e: UserNotFoundException     => NotFound(e.getMessage)
    case e: UsernameNotFoundException => NotFound(e.getMessage)
    case e: GameNotFoundException     => NotFound(e.getMessage)
    case e: GameInterloperException   => Forbidden(e.getMessage)
    case e: NotGameCreatorException   => Forbidden(e.getMessage)
    case e: GameException             => Conflict(e.getMessage)
    case _                            => InternalServerError()
  }
