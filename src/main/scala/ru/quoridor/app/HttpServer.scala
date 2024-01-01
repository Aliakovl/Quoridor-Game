package ru.quoridor.app

import org.http4s.HttpApp
import ru.quoridor.config.Address
import zio.*

import javax.net.ssl.SSLContext

trait HttpServer[-R, +E, +S]:
  def start: ZIO[R with Scope, E, S]

object HttpServer:
  def live(httpApp: HttpApp[EnvTask]): ZLayer[
    Env with Scope with SSLContext with Address,
    Throwable,
    BlazeServer
  ] =
    ZLayer(for {
      address <- ZIO.service[Address]
      sslContext <- ZIO.service[SSLContext]
    } yield new BlazeServer(address, sslContext, httpApp))
