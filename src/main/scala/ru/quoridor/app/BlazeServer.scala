package ru.quoridor.app

import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import ru.quoridor.config.Address
import sttp.apispec.openapi.OpenAPI
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.ztapir.ZServerEndpoint
import zio.*
import zio.interop.catz.*

import javax.net.ssl.SSLContext

class BlazeServer(
    address: Address,
    sslContext: SSLContext,
    routes: List[ZServerEndpoint[Env, ZioStreams]],
    openAPI: OpenAPI
) extends HttpServer[Env, Throwable, Server]:

  private val apiRoutes: HttpRoutes[EnvTask] = ZHttp4sServerInterpreter[Env]()
    .from(routes)
    .toRoutes

  private val openAPIRoutes: HttpRoutes[EnvTask] = ZHttp4sServerInterpreter()
    .from(SwaggerUI[EnvTask](openAPI.toYaml))
    .toRoutes

  override val start: ZIO[Env with Scope, Throwable, Server] =
    BlazeServerBuilder[EnvTask]
      .bindHttp(address.port, address.host)
      .withSslContext(sslContext)
      .enableHttp2(true)
      .withHttpApp((apiRoutes <+> openAPIRoutes).orNotFound)
      .resource
      .toScopedZIO
