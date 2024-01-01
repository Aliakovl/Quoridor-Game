package ru.quoridor.app

import cats.implicits.*
import io.getquill.jdbczio.Quill
import org.http4s.HttpRoutes
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
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.interop.catz.*
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.*

import javax.net.ssl.SSLContext

object QuoridorApp extends ZIOApp:
  private val apiRoutes: HttpRoutes[EnvTask] =
    ZHttp4sServerInterpreter[Env]()
      .from(GameAPI[Env] ++ AuthorizationAPI[Env] ++ StreamAPI[Env])
      .toRoutes

  private val layers = ZLayer
    .make[Env](
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

  override def run: ZIO[Server with ZIOAppArgs with Scope, Any, Any] = ZIO.never

  override val bootstrap: ZLayer[ZIOAppArgs, Throwable, Server] = ZLayer.make[Server](
    Runtime.removeDefaultLoggers,
    Slf4jBridge.initialize,
    Configuration.live,
    Address.live,
    SSLKeyStore.live,
    SSLProvider.live,
    layers,
    HttpServer.live((apiRoutes <+> Swagger.docs).orNotFound),
    Scope.default
  )

  override type Environment = Server

  override val environmentTag: EnvironmentTag[Server] = EnvironmentTag[Server]
