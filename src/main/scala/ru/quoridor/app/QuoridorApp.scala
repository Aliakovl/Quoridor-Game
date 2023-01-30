package ru.quoridor.app

import cats.implicits._
import io.circe.Encoder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Response}
import ru.quoridor.ExceptionResponse
import ru.quoridor.api.WSGameApi
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.swagger.SwaggerUI
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import zio.interop.catz._
import zio.{ExitCode, Scope, Task, ZIO, ZIOAppArgs, ZIOAppDefault}

object QuoridorApp extends ZIOAppDefault {
  private val openApi = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI[Task](
    QuoridorGame.api,
    "Quoridor game server",
    "0.0.1"
  )

  private val swagger =
    Http4sServerInterpreter[Task]().toRoutes(SwaggerUI[Task](openApi.toYaml))

  private val httpApp: HttpRoutes[Task] = QuoridorGame.apiRoutes <+> swagger

  implicit val jsonEncode: Encoder[ExceptionResponse] =
    Encoder.forProduct1("errorMessage")(_.errorMessage)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, ExitCode] =
    BlazeServerBuilder[Task]
      .bindHttp(
        QuoridorGame.appConfig.address.port,
        QuoridorGame.appConfig.address.host
      )
      .withHttpWebSocketApp({ wsb =>
        Router[Task](
          "/" -> httpApp,
          "ws" -> new WSGameApi(wsb, QuoridorGame.gameService).routeWs
            .handleError { _ =>
              Response(InternalServerError).withEntity(
                ExceptionResponse("Something went wrong!")
              )
            }
        ).orNotFound
      })
      .serve
      .compile
      .drain
      .exitCode
}
