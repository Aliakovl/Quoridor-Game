package ru.quoridor.api

import io.circe.{Decoder, Encoder}
import sttp.tapir.ztapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*
import io.circe.generic.auto.*
import ru.quoridor.auth.AuthorizationService
import ru.quoridor.auth.AuthorizationService.validate
import ru.quoridor.auth.model.AccessToken
import ru.quoridor.model.game.Game
import ru.quoridor.services.GameService
import ru.quoridor.services.GameService.subscribeOnGame
import ru.utils.TapirExtensions
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.given
import sttp.model.HeaderNames
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.json.circe.jsonBody

object StreamAPI extends TapirExtensions:
  def apply[
      Env <: GameService with AuthorizationService
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
      .out(zioStreamBody[Game])
      .serverLogic { claimData => subscribeOnGame }
