package ru.quoridor.app

import io.getquill.jdbczio.Quill
import ru.quoridor.api.{
  AuthorizationEndpoints,
  AuthorizationServerEndpoints,
  BaseEndpoints,
  Endpoints,
  GameEndpoints,
  GameServerEndpoints,
  StreamEndpoints,
  StreamServerEndpoints
}
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
    ZLayer.make[BlazeServer](
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
      GamePubSub.live,
      Address.live,
      SSLKeyStore.live,
      SSLProvider.live,
      HttpServer.live,
      BaseEndpoints.live,
      AuthorizationEndpoints.live,
      GameEndpoints.live,
      StreamEndpoints.live,
      AuthorizationServerEndpoints.live,
      GameServerEndpoints.live,
      StreamServerEndpoints.live,
      Endpoints.live
    )

  private val server: ZIO[Scope with BlazeServer, Throwable, Unit] =
    ZIO.serviceWithZIO[BlazeServer](_.start).unit

  override def run: ZIO[ZIOAppArgs, Nothing, ExitCode] = ZIO
    .scoped {
      server <* ZIO.never
    }
    .provideLayer(layers)
    .exitCode

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers ++ Slf4jBridge.initialize
