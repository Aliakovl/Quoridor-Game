package ru.quoridor.app

import cats.implicits._
import io.circe.Encoder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Response}
import ru.quoridor.api.{ExceptionResponse, WSGameApi}
import ru.quoridor.app.QuoridorGame.EnvTask
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.storage.{
  DataBase,
  GameStorage,
  ProtoGameStorage,
  UserStorage
}
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.swagger.SwaggerUI
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import zio.interop.catz._
import zio.{ExitCode, ZIO, ZIOAppDefault}

object QuoridorApp extends ZIOAppDefault {
  private val openApi =
    OpenAPIDocsInterpreter().serverEndpointsToOpenAPI[EnvTask](
      QuoridorGame.api,
      "Quoridor game server",
      "0.0.1"
    )

  private val swagger =
    Http4sServerInterpreter[EnvTask]()
      .toRoutes(SwaggerUI[EnvTask](openApi.toYaml))

  private val httpApp: HttpRoutes[EnvTask] =
    QuoridorGame.apiRoutes <+> swagger

  implicit val jsonEncode: Encoder[ExceptionResponse] =
    Encoder.forProduct1("errorMessage")(_.errorMessage)

  override def run: ZIO[Any, Any, ExitCode] = ZIO
    .serviceWithZIO[AppConfig] { appConfig =>
      BlazeServerBuilder[EnvTask]
        .bindHttp(
          appConfig.address.port,
          appConfig.address.host
        )
        .withHttpWebSocketApp({ wsb =>
          Router[EnvTask](
            "/" -> httpApp,
            "ws" -> new WSGameApi(wsb).routeWs
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
    .provide(
      QuoridorGame.appConfigLayer,
      DataBase.live,
      ProtoGameStorage.live,
      GameStorage.live,
      UserStorage.live,
      GameCreator.live,
      GameService.live,
      UserService.live
    )

}
