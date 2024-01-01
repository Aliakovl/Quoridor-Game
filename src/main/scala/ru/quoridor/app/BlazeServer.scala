package ru.quoridor.app

import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import ru.quoridor.api.Endpoints
import ru.quoridor.config.Address
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.*
import zio.interop.catz.*

import javax.net.ssl.SSLContext

class BlazeServer(
    address: Address,
    sslContext: SSLContext,
    endpoints: Endpoints
) extends HttpServer[Any, Throwable, Server]:

  private val apiRoutes: HttpRoutes[Task] = ZHttp4sServerInterpreter[Any]()
    .from(endpoints.endpoints)
    .toRoutes

  override val start: ZIO[Scope, Throwable, Server] =
    BlazeServerBuilder[Task]
      .bindHttp(address.port, address.host)
      .withSslContext(sslContext)
      .enableHttp2(true)
      .withHttpApp(apiRoutes.orNotFound)
      .resource
      .toScopedZIO
