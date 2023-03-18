package ru.quoridor.app

import org.http4s.HttpRoutes
import ru.quoridor.api.{AuthorizationAPI, GameAPI}
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI

object Swagger {
  private val openApi =
    OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(
      GameAPI[Env] ++ AuthorizationAPI[Env],
      "Quoridor game api",
      "0.1.0"
    )

  val docs: HttpRoutes[EnvTask] =
    ZHttp4sServerInterpreter()
      .from(SwaggerUI[EnvTask](openApi.toYaml))
      .toRoutes
}
