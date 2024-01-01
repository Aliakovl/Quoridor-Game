package ru.quoridor.app

import ru.quoridor.config.Address
import sttp.apispec.openapi.OpenAPI
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.ZServerEndpoint
import zio.*

import javax.net.ssl.SSLContext

trait HttpServer[-R, +E, +S]:
  def start: ZIO[R with Scope, E, S]

object HttpServer:
  def live(
      routes: List[ZServerEndpoint[Env, ZioStreams]],
      openAPI: OpenAPI
  ): ZLayer[SSLContext with Address, Nothing, BlazeServer] =
    ZLayer(for {
      address <- ZIO.service[Address]
      sslContext <- ZIO.service[SSLContext]
    } yield new BlazeServer(address, sslContext, routes, openAPI))
