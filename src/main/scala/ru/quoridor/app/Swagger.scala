package ru.quoridor.app

import ru.quoridor.api.{AuthorizationAPI, GameAPI, StreamAPI}
import sttp.apispec.openapi.{Info, OpenAPI}
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter

object Swagger:
  val openApi: OpenAPI =
    OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(
      GameAPI[Env] ++ AuthorizationAPI[Env] ++ StreamAPI[Env],
      Info(
        "Quoridor Game Api",
        "0.1.0"
      )
    )
