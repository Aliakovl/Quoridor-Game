package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.auth.model.{AccessToken, ClaimData}
import dev.aliakovl.quoridor.model.game.Game
import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.utils.tagging.Tagged.given
import dev.aliakovl.utils.tapir.TapirExtensions
import sttp.tapir.ztapir.*
import sttp.model.HeaderNames
import sttp.capabilities.zio.ZioStreams
import zio.{ZLayer, stream}

class StreamEndpoints(base: BaseEndpoints) extends TapirExtensions:
  val sseEndpoint: ZPartialServerEndpoint[Any, AccessToken, ClaimData, ID[
    Game
  ], Throwable, stream.Stream[Nothing, Game], ZioStreams] =
    base.secureEndpoint.get
      .in("stream" / "api" / "v1")
      .in("game" / path[ID[Game]]("gameId"))
      .out(header(HeaderNames.CacheControl, "no-store"))
      .out(messageStreamBody[Nothing, Game])

object StreamEndpoints:
  val live: ZLayer[BaseEndpoints, Nothing, StreamEndpoints] =
    ZLayer.fromFunction(new StreamEndpoints(_))
