package ru.quoridor.app

import org.http4s.HttpRoutes
import ru.quoridor.app.QuoridorGame.EnvTask
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI

object Swagger {
  private val openApi =
    OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(
      QuoridorGame.api,
      "Quoridor game server",
      "0.0.1"
    )

  val routes: HttpRoutes[EnvTask] =
    ZHttp4sServerInterpreter()
      .from(SwaggerUI[EnvTask](openApi.toYaml))
      .toRoutes
}
