package ru.quoridor.app

import org.http4s.HttpApp
import org.http4s.server.Server
import ru.quoridor.config.Address
import zio.*

import javax.net.ssl.SSLContext

trait HttpServer[-R, +E, +S]:
  def start: ZIO[R with Scope, E, S]

object HttpServer:
  def live(
      httpApp: HttpApp[EnvTask]
  ): ZLayer[Env with Scope with SSLContext with Address, Throwable, Server] =
    ZLayer(for {
      address <- ZIO.service[Address]
      sslContext <- ZIO.service[SSLContext]
      server <- new BlazeServer(address, sslContext, httpApp).start
    } yield server)
