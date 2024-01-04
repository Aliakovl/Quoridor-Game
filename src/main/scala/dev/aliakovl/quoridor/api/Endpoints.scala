package dev.aliakovl.quoridor.api

import sttp.apispec.openapi.Info
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.{Task, URLayer, ZLayer}

class Endpoints(
    authorizationServerEndpoints: AuthorizationServerEndpoints,
    gameServerEndpoint: GameServerEndpoints,
    streamServerEndpoint: StreamServerEndpoints
):
  val endpoints: List[ZServerEndpoint[Any, ZioStreams]] = {
    val api =
      authorizationServerEndpoints.endpoints ++ gameServerEndpoint.endpoints ++ streamServerEndpoint.endpoints
    val docs = docsEndpoints(api)
    api ++ docs
  }

  private def docsEndpoints(
      apiEndpoints: List[ZServerEndpoint[Any, ZioStreams]]
  ): List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](
      apiEndpoints,
      Info(
        "Quoridor Game Api",
        "0.1.0"
      )
    )

object Endpoints:
  val live: URLayer[
    AuthorizationServerEndpoints & GameServerEndpoints & StreamServerEndpoints,
    Endpoints
  ] = ZLayer.fromFunction(new Endpoints(_, _, _))
