package dev.aliakovl.quoridor.app

import dev.aliakovl.quoridor.api.*
import dev.aliakovl.quoridor.auth.store.RefreshTokenStore
import dev.aliakovl.quoridor.auth.*
import dev.aliakovl.quoridor.config.*
import dev.aliakovl.quoridor.services.{GameCreator, GameService, UserService}
import dev.aliakovl.quoridor.dao.quill.QuillContext
import dev.aliakovl.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import dev.aliakovl.quoridor.pubsub.*
import dev.aliakovl.utils.SSLProvider
import io.getquill.jdbczio.Quill
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

  private val server: ZIO[Scope & BlazeServer, Throwable, Unit] =
    ZIO.serviceWithZIO[BlazeServer](_.start).unit

  override def run: ZIO[ZIOAppArgs, Nothing, ExitCode] = ZIO
    .scoped {
      server <* ZIO.never
    }
    .provideLayer(layers)
    .exitCode

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers ++ Slf4jBridge.initialize
