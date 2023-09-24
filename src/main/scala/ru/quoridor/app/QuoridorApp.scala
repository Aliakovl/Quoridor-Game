package ru.quoridor.app

import cats.implicits.*
import io.getquill.jdbczio.Quill
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits.*
import org.http4s.HttpRoutes
import org.http4s.server.websocket.WebSocketBuilder2
import ru.quoridor.api.{AuthorizationAPI, GameAPI, GameAsyncAPI}
import ru.quoridor.auth.store.RefreshTokenStore
import ru.quoridor.auth.*
import ru.quoridor.auth.model.RefreshToken
import ru.quoridor.auth.store.redis.{*, given}
import ru.quoridor.config.*
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import ru.quoridor.model.User
import ru.quoridor.model.game.Game
import ru.utils.tagging.ID
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.interop.catz.*
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.*
import ru.utils.tagging.Tagged

import java.util.UUID

object QuoridorApp extends ZIOAppDefault:
  private val apiRoutes: HttpRoutes[EnvTask] =
    ZHttp4sServerInterpreter[Env]()
      .from(GameAPI[Env] ++ AuthorizationAPI[Env])
      .toRoutes

  private val asyncApiRoutes: WebSocketBuilder2[EnvTask] => HttpRoutes[EnvTask] =
    ZHttp4sServerInterpreter[Env]().fromWebSocket(GameAsyncAPI[Env]).toRoutes

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

  private lazy val layers = ZLayer
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
      RedisStore.live[RefreshToken, ID[User]],
      RefreshTokenStore.live,
      AccessService.live,
      AuthorizationService.live,
      AuthenticationService.live,
      ZLayer(Hub.sliding[Game](1000))
    )
