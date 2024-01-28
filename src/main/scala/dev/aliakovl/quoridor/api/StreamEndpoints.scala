package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.auth.model.{AccessToken, ClaimData}
import dev.aliakovl.quoridor.model.Game
import dev.aliakovl.quoridor.services.model.GameResponse
import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.utils.tagging.Tagged.given
import dev.aliakovl.utils.tapir.TapirExtensions
import sttp.tapir.ztapir.*
import sttp.model.HeaderNames
import sttp.capabilities.zio.ZioStreams
import zio.{URLayer, ZLayer}
import zio.stream.Stream

class StreamEndpoints(base: BaseEndpoints) extends TapirExtensions:
  val sseEndpoint: ZPartialServerEndpoint[Any, AccessToken, ClaimData, ID[
    Game
  ], ErrorResponse, Stream[Nothing, GameResponse], ZioStreams] =
    base.secureEndpoint.get
      .tag("Game")
      .name("game-events")
      .summary("Subscribe on game events")
      .in("stream" / "api" / "v1")
      .in("game" / path[ID[Game]]("gameId"))
      .out(header(HeaderNames.CacheControl, "no-store"))
      .out(messageStreamBody[Nothing, GameResponse])

object StreamEndpoints:
  val live: URLayer[BaseEndpoints, StreamEndpoints] =
    ZLayer.fromFunction(new StreamEndpoints(_))
