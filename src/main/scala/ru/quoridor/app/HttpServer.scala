package ru.quoridor.app

import ru.quoridor.api.Endpoints
import ru.quoridor.config.Address
import zio.*

import javax.net.ssl.SSLContext

trait HttpServer[-R, +E, +S]:
  def start: ZIO[R & Scope, E, S]

object HttpServer:
  def live: URLayer[Address & SSLContext & Endpoints, BlazeServer] =
    ZLayer.fromFunction(new BlazeServer(_, _, _))
