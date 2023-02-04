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
import ru.quoridor.app.QuoridorGame.Env
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
import zio.{ExitCode, RIO, ZIO, ZIOAppDefault}

object QuoridorApp extends ZIOAppDefault {
  private val openApi =
    OpenAPIDocsInterpreter().serverEndpointsToOpenAPI[RIO[Env, _]](
      QuoridorGame.api,
      "Quoridor game server",
      "0.0.1"
    )

  private val swagger =
    Http4sServerInterpreter[RIO[Env, _]]()
      .toRoutes(SwaggerUI[RIO[Env, _]](openApi.toYaml))

  private val httpApp: HttpRoutes[RIO[Env, _]] =
    QuoridorGame.apiRoutes <+> swagger

  implicit val jsonEncode: Encoder[ExceptionResponse] =
    Encoder.forProduct1("errorMessage")(_.errorMessage)

  override def run: ZIO[Any, Any, ExitCode] = {
    for {
      appConfig <- ZIO.service[AppConfig]
      gameService <- ZIO.service[GameService]
      exitCode <- BlazeServerBuilder[RIO[Env, _]]
        .bindHttp(
          appConfig.address.port,
          appConfig.address.host
        )
        .withHttpWebSocketApp({ wsb =>
          Router[RIO[Env, _]](
            "/" -> httpApp,
            "ws" -> new WSGameApi(wsb, gameService).routeWs
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
    } yield exitCode
  }.provide(
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
