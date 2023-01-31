package ru.quoridor.app

import cats.effect.{ExitCode, IO, IOApp}
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
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.server.http4s.Http4sServerInterpreter


object QuoridorApp extends IOApp {
  val openApi = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI[IO](
    QuoridorGame.api,
    "Quoridor game server",
    "0.0.1"
  )

  val swagger = Http4sServerInterpreter[IO]().toRoutes(SwaggerUI[IO](openApi.toYaml))

  val httpApp: HttpRoutes[IO] = QuoridorGame.apiRoutes <+> swagger

  implicit val jsonEncode: Encoder[ExceptionResponse] = Encoder.forProduct1("errorMessage")(_.message)

  override def run(args: List[String]): IO[ExitCode] = BlazeServerBuilder[IO]
    .bindHttp(QuoridorGame.appConfig.address.port, QuoridorGame.appConfig.address.host)
    .withHttpWebSocketApp({ wsb =>
      Router[IO] (
        "/" -> httpApp,
        "ws" -> new WSGameApi(wsb, QuoridorGame.gameService).routeWs.handleError{ error =>
          Response(InternalServerError).withEntity(ExceptionResponse(error))
        }
      ).orNotFound
    })
    .serve
    .compile
    .drain
    .as(ExitCode.Success)
}