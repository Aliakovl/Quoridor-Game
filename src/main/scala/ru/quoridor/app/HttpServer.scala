package ru.quoridor.app

import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import ru.quoridor.config.Address
import zio.*
import zio.interop.catz.*

import javax.net.ssl.SSLContext

trait HttpServer[R] {
  def start: ZIO[R with Scope, Throwable, Server]
}

class BlazeServer(
    address: Address,
    sslContext: SSLContext,
    httpApp: HttpApp[EnvTask]
) extends HttpServer[Env] {
  override def start: ZIO[Env with Scope, Throwable, Server] =
    BlazeServerBuilder[EnvTask]
      .bindHttp(address.port, address.host)
      .withSslContext(sslContext)
      .enableHttp2(true)
      .withHttpApp(httpApp)
      .resource
      .toScopedZIO
}

object HttpServer:
  def live(
      httpApp: HttpApp[EnvTask]
  ): ZLayer[Env with Scope with SSLContext with Address, Throwable, Server] =
    ZLayer(for {
      address <- ZIO.service[Address]
      sslContext <- ZIO.service[SSLContext]
      server <- new BlazeServer(address, sslContext, httpApp).start
    } yield server)
