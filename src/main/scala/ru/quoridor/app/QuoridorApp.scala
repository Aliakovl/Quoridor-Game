package ru.quoridor.app

import io.getquill.jdbczio.Quill
import org.http4s.server.Server
import ru.quoridor.api.{AuthorizationAPI, GameAPI, StreamAPI}
import ru.quoridor.auth.store.RefreshTokenStore
import ru.quoridor.auth.*
import ru.quoridor.config.*
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import ru.quoridor.pubsub.*
import ru.utils.SSLProvider
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.*

import javax.net.ssl.SSLContext

object QuoridorApp extends ZIOAppDefault:
  private val layers =
    ZLayer.make[Env](
      Auth.live,
      TokenKeys.live,
      TokenStore.live,
      PubSubRedis.live,
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
      RefreshTokenStore.live,
      AccessService.live,
      AuthorizationService.live,
      AuthenticationService.live,
      GamePubSub.live
    )

  private val server = ZIO
    .serviceWithZIO[BlazeServer](_.start)
    .provide(
      Runtime.removeDefaultLoggers,
      Slf4jBridge.initialize,
      Configuration.live,
      Address.live,
      SSLKeyStore.live,
      SSLProvider.live,
      layers,
      HttpServer.live(
        GameAPI[Env] ++ AuthorizationAPI[Env] ++ StreamAPI[Env],
        Swagger.openApi
      ),
      Scope.default
    )

  override def run: ZIO[ZIOAppArgs, Throwable, Nothing] =
    server *> ZIO.never
