package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.services.GameService
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import zio.{URLayer, ZLayer}

class StreamServerEndpoints(
    gameService: GameService,
    streamEndpoint: StreamEndpoints
):
  val sseServerEndpoint: ZServerEndpoint[Any, ZioStreams] =
    streamEndpoint.sseEndpoint
      .serverLogic { claimData => gameId =>
        gameService.subscribeOnGame(gameId).map(_.orDie)
      }

  val endpoints: List[ZServerEndpoint[Any, ZioStreams]] = List(
    sseServerEndpoint
  )

object StreamServerEndpoints:
  val live: URLayer[GameService & StreamEndpoints, StreamServerEndpoints] =
    ZLayer.fromFunction(new StreamServerEndpoints(_, _))
