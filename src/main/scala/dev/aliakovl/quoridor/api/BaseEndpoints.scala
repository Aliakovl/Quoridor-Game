package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.api.BaseEndpoints.defaultErrorOutputs
import dev.aliakovl.quoridor.auth.AuthorizationService
import dev.aliakovl.quoridor.auth.model.{AccessToken, ClaimData}
import sttp.tapir.json.circe.jsonBody
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.{EndpointOutput, PublicEndpoint}
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.ztapir.*
import zio.{IO, URLayer, ZLayer}

class BaseEndpoints(authorizationService: AuthorizationService):
  val secureEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    Unit,
    ErrorResponse,
    Unit,
    Any
  ] = endpoint
    .errorOut(defaultErrorOutputs)
    .securityIn(auth.bearer[AccessToken]())
    .zServerSecurityLogic[Any, ClaimData](handleAuthorization)

  val publicEndpoint: PublicEndpoint[Unit, ErrorResponse, Unit, Any] = endpoint
    .errorOut(defaultErrorOutputs)

  private def handleAuthorization(
      accessToken: AccessToken
  ): IO[ErrorResponse, ClaimData] = {
    authorizationService
      .validate(accessToken)
      .mapError(e => Unauthorized(e.getMessage))
  }

object BaseEndpoints:
  val live: URLayer[AuthorizationService, BaseEndpoints] =
    ZLayer.fromFunction(new BaseEndpoints(_))

  val defaultErrorOutputs: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] =
    oneOf[ErrorResponse](
      oneOfVariant(
        statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized])
      ),
      oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest])),
      oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound])),
      oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[Forbidden])),
      oneOfVariant(
        statusCode(StatusCode.InternalServerError).and(
          jsonBody[InternalServerError]
        )
      )
    )
