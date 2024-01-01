package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.auth.AuthorizationService
import dev.aliakovl.quoridor.auth.model.{AccessToken, ClaimData}
import sttp.tapir.json.circe.jsonBody
import io.circe.generic.auto.*
import sttp.tapir.PublicEndpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.ztapir.*
import zio.ZLayer

class BaseEndpoints(authorizationService: AuthorizationService):
  val secureEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    Unit,
    Throwable,
    Unit,
    Any
  ] = endpoint
    .securityIn(auth.bearer[AccessToken]())
    .errorOut(jsonBody[ExceptionResponse] and statusCode)
    .mapErrorOut(er => new Throwable(er._1.errorMessage))(
      ExceptionResponse(_)
    )
    .zServerSecurityLogic(authorizationService.validate)

  val publicEndpoint: PublicEndpoint[Unit, Throwable, Unit, Any] = endpoint
    .errorOut(jsonBody[ExceptionResponse] and statusCode)
    .mapErrorOut(er => new Throwable(er._1.errorMessage))(
      ExceptionResponse(_)
    )

object BaseEndpoints:
  val live: ZLayer[AuthorizationService, Nothing, BaseEndpoints] =
    ZLayer.fromFunction(new BaseEndpoints(_))
