package dev.aliakovl.quoridor.app

import dev.aliakovl.quoridor.api.Endpoints
import dev.aliakovl.quoridor.config.{Address, Configuration}
import zio.*

import javax.net.ssl.SSLContext

trait HttpServer[-R, +E, +S]:
  def start: ZIO[R & Scope, E, S]

object HttpServer:
  val live: URLayer[Address & SSLContext & Endpoints, BlazeServer] =
    ZLayer.fromFunction(new BlazeServer(_, _, _))

  val configuredLive: URLayer[Endpoints & SSLContext, BlazeServer] =
    Configuration.address >>> live
