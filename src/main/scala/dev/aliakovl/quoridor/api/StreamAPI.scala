package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.api.data.ExceptionResponse
import dev.aliakovl.quoridor.api.data.Responses.GameResponse
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto.*
import sttp.tapir.ztapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*
import dev.aliakovl.quoridor.auth.AuthorizationService
import dev.aliakovl.quoridor.auth.AuthorizationService.validate
import dev.aliakovl.quoridor.auth.model.AccessToken
import dev.aliakovl.quoridor.codec.circe.Orientation.given
import dev.aliakovl.quoridor.codec.circe.Side.given
import dev.aliakovl.quoridor.engine.Game
import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.utils.tagging.Tagged.given
import dev.aliakovl.utils.tapir.TapirExtensions
import sttp.model.HeaderNames
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.json.circe.jsonBody

object StreamAPI extends TapirExtensions:
  def apply[
      Env <: GameApiService with AuthorizationService
  ]: List[ZServerEndpoint[Env, ZioStreams]] = List(
    sse.widen[Env]
  )

  private val baseEndpoint =
    endpoint
      .in("stream" / "api" / "v1")
      .securityIn(auth.bearer[AccessToken]())
      .errorOut(jsonBody[ExceptionResponse] and statusCode)
      .mapErrorOut(er => new Throwable(er._1.errorMessage))(
        ExceptionResponse(_)
      )
      .zServerSecurityLogic { accessToken =>
        validate(accessToken)
      }

  private val sse =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId"))
      .out(header(HeaderNames.CacheControl, "no-store"))
      .out(messageStreamBody[Nothing, GameResponse])
      .serverLogic { claimData => gameId =>
        GameApiService.subscribeOnGame(claimData)(gameId).map(_.orDie)
      }
