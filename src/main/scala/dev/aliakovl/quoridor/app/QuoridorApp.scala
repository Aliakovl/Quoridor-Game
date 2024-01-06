package dev.aliakovl.quoridor.app

import dev.aliakovl.quoridor.api.*
import dev.aliakovl.quoridor.auth.store.*
import dev.aliakovl.quoridor.auth.*
import dev.aliakovl.quoridor.services.*
import dev.aliakovl.quoridor.dao.quill.QuillContext
import dev.aliakovl.quoridor.dao.*
import dev.aliakovl.quoridor.pubsub.*
import dev.aliakovl.utils.SSLProvider
import zio.*
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

import javax.net.ssl.SSLContext

object QuoridorApp extends ZIOAppDefault:
  private val layers =
    ZLayer.make[BlazeServer](
      QuillContext.configuredLive,
      ProtoGameDaoLive.live,
      GameDaoLive.live,
      UserDaoLive.live,
      GameCreatorLive.live,
      GameServiceLive.live,
      UserServiceLive.live,
      HashingServiceLive.live,
      AccessServiceLive.configuredLive,
      AuthorizationServiceLive.configuredLive,
      AuthenticationServiceLive.live,
      RefreshTokenStoreLive.configuredLive,
      GamePubSubLive.configuredLive,
      BaseEndpoints.live,
      AuthorizationEndpoints.live,
      GameEndpoints.live,
      StreamEndpoints.live,
      AuthorizationServerEndpoints.live,
      GameServerEndpoints.live,
      StreamServerEndpoints.live,
      Endpoints.live,
      SSLProvider.configuredLive,
      BlazeServer.configuredLive
    )

  private val server: ZIO[Scope & BlazeServer, Throwable, Unit] =
    ZIO.serviceWithZIO[BlazeServer](_.start).unit

  override def run: ZIO[ZIOAppArgs, Nothing, ExitCode] = ZIO
    .scoped(server <* ZIO.never)
    .provideLayer(layers)
    .exitCode

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    SLF4J.slf4j(LogFormat.colored)
