package ru.quoridor.app

import cats.implicits._
import io.getquill.jdbczio.Quill
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.HttpRoutes
import ru.quoridor.api.{AuthorizationAPI, GameAPI, GameAsyncAPI}
import ru.quoridor.auth.store.RefreshTokenStore
import ru.quoridor.auth._
import ru.quoridor.auth.model.RefreshToken
import ru.quoridor.auth.store.redis._
import ru.quoridor.config._
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import ru.quoridor.model.User
import ru.quoridor.model.game.Game
import ru.utils.tagging.Id
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.interop.catz._
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.{ExitCode, Hub, ULayer, ZIO, ZIOAppDefault, ZLayer}

object QuoridorApp extends ZIOAppDefault {
  private val apiRoutes: HttpRoutes[EnvTask] =
    ZHttp4sServerInterpreter()
      .from(GameAPI[Env] ++ AuthorizationAPI[Env])
      .toRoutes

  private val asyncApiRoutes =
    ZHttp4sServerInterpreter().fromWebSocket(GameAsyncAPI[Env]).toRoutes

  override def run: ZIO[Any, Any, ExitCode] = ZIO
    .serviceWithZIO[Address] { case Address(host, port) =>
      BlazeServerBuilder[EnvTask]
        .bindHttp(port, host)
        .withHttpWebSocketApp({ wsb =>
          (asyncApiRoutes(wsb) <+> apiRoutes <+> Swagger.docs).orNotFound
        })
        .serve
        .compile
        .drain
        .exitCode
    }
    .provide(
      Slf4jBridge.initialize,
      Address.live,
      Configuration.live,
      layers
    )

  private val layers: ULayer[Env] = ZLayer
    .make[Env](
      Auth.live,
      TokenKeys.live,
      TokenStore.live,
      Configuration.live,
      Quill.DataSource.fromPrefix("hikari"),
      QuillContext.live,
      ProtoGameDao.live,
      GameDao.live,
      UserDao.live,
      GameCreator.live,
      GameService.live,
      UserService.live,
      HashingService.live,
      RedisStore.live[RefreshToken, Id[User]],
      RefreshTokenStore.live,
      AccessService.live,
      AuthorizationService.live,
      AuthenticationService.live,
      ZLayer(Hub.sliding[Game](1000))
    )
    .orDie
}
