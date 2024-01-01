package ru.quoridor.app

import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import ru.quoridor.config.Address
import zio.*
import zio.interop.catz.*

import javax.net.ssl.SSLContext

class BlazeServer(
    address: Address,
    sslContext: SSLContext,
    httpApp: HttpApp[EnvTask]
) extends HttpServer[Env, Throwable, Server]:
  override def start: ZIO[Env with Scope, Throwable, Server] =
    BlazeServerBuilder[EnvTask]
      .bindHttp(address.port, address.host)
      .withSslContext(sslContext)
      .enableHttp2(true)
      .withHttpApp(httpApp)
      .resource
      .toScopedZIO
