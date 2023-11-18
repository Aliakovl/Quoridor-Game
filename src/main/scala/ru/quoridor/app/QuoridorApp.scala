package ru.quoridor.app

import cats.implicits.*
import io.getquill.jdbczio.Quill
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits.*
import org.http4s.HttpRoutes
import ru.quoridor.api.{AuthorizationAPI, GameAPI, StreamAPI}
import ru.quoridor.auth.store.RefreshTokenStore
import ru.quoridor.auth.*
import ru.quoridor.config.*
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import ru.utils.SSLProvider
import ru.utils.ZIOExtensions.*
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.interop.catz.*
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.*
import ru.quoridor.pubsub.*

import javax.net.ssl.SSLContext

object QuoridorApp extends ZIOAppDefault:
  private val apiRoutes: HttpRoutes[EnvTask] =
    ZHttp4sServerInterpreter[Env]()
      .from(GameAPI[Env] ++ AuthorizationAPI[Env] ++ StreamAPI[Env])
      .toRoutes

  override def run: ZIO[Any, Any, ExitCode] = (for {
    Address(host, port) <- ZIO.service[Address]
    sslContext <- ZIO.service[SSLContext]
    exitCode <- BlazeServerBuilder[EnvTask]
      .bindHttp(port, host)
      .withSslContext(sslContext)
      .enableHttp2(true)
      .withHttpApp((apiRoutes <+> Swagger.docs).orNotFound)
      .serve
      .compile
      .drain
      .exitCode
  } yield exitCode).provide(
    Slf4jBridge.initialize,
    Configuration.live,
    Address.live,
    SSLKeyStore.live,
    SSLProvider.live,
    layers
  )

  private lazy val layers = ZLayer
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
      GamePubSub.live,
      Scope.default
    )
