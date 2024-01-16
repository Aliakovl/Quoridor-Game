package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.api.ErrorMapping.defaultErrorsMapping
import dev.aliakovl.quoridor.services.GameService
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import zio.{URLayer, ZLayer}

import scala.util.chaining.*

class StreamServerEndpoints(
    gameService: GameService,
    streamEndpoint: StreamEndpoints
):
  private val sseServerEndpoint: ZServerEndpoint[Any, ZioStreams] =
    streamEndpoint.sseEndpoint
      .serverLogic { claimData => gameId =>
        gameService
          .subscribeOnGame(gameId)
          .map(_.orDie)
          .pipe(defaultErrorsMapping)
      }

  val endpoints: List[ZServerEndpoint[Any, ZioStreams]] = List(
    sseServerEndpoint
  )

object StreamServerEndpoints:
  val live: URLayer[GameService & StreamEndpoints, StreamServerEndpoints] =
    ZLayer.fromFunction(new StreamServerEndpoints(_, _))
