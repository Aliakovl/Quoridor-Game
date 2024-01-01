package ru.quoridor.api

import sttp.tapir.ztapir.*
import ru.quoridor.auth.model.{AccessToken, ClaimData}
import ru.quoridor.model.game.Game
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.given
import ru.utils.tapir.TapirExtensions
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
