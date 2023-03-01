package ru.quoridor.app

import cats.implicits._
import io.circe.Encoder
import io.getquill.jdbczio.Quill
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Response}
import ru.quoridor.api.{ExceptionResponse, WSGameApi}
import ru.quoridor.app.QuoridorGame.EnvTask
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.storage.quillInst.QuillContext
import ru.quoridor.storage.{
  DataBase,
  GameStorage,
  ProtoGameStorage,
  UserStorage
}
import zio.interop.catz._
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.{ExitCode, ZIO, ZIOAppDefault}

object QuoridorApp extends ZIOAppDefault {
  private val httpApp: HttpRoutes[EnvTask] =
    QuoridorGame.apiRoutes <+> Swagger.routes

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
      Quill.DataSource.fromPrefix("hikari"),
      QuillContext.live,
      ProtoGameStorage.live,
      GameStorage.live,
      UserStorage.live,
      GameCreator.live,
      GameService.live,
      UserService.live,
      Slf4jBridge.initialize
    )

}
