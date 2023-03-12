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
import ru.quoridor.app.QuoridorGame.{Env, EnvTask}
import ru.quoridor.auth.store.RefreshTokenStore
import ru.quoridor.auth.{
  AccessService,
  AuthenticationService,
  AuthorizationService,
  HashingService
}
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import zio.interop.catz._
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.{ExitCode, ULayer, ZIO, ZIOAppDefault, ZLayer}

object QuoridorApp extends ZIOAppDefault {
  private val httpApp: HttpRoutes[EnvTask] =
    QuoridorGame.apiRoutes <+> Swagger.routes

  implicit val jsonEncode: Encoder[ExceptionResponse] =
    Encoder.forProduct1("errorMessage")(_.errorMessage)

  private val layers: ULayer[Env] = ZLayer
    .make[Env](
      Quill.DataSource.fromPrefix("hikari"),
      QuillContext.live,
      ProtoGameDao.live,
      GameDao.live,
      UserDao.live,
      GameCreator.live,
      GameService.live,
      UserService.live,
      HashingService.live,
      RefreshTokenStore.live,
      AccessService.live,
      AuthorizationService.live,
      AuthenticationService.live
    )
    .orDie

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
    .provideLayer(
      QuoridorGame.appConfigLayer ++ (Slf4jBridge.initialize >>> layers)
    )
}
