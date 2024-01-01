package ru.quoridor.api

import ru.quoridor.services.GameService
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import zio.ZLayer

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
  val live: ZLayer[
    GameService with StreamEndpoints,
    Nothing,
    StreamServerEndpoints
  ] = ZLayer.fromFunction(new StreamServerEndpoints(_, _))
