package dev.aliakovl.quoridor.app

import zio.*

trait HttpServer[-R, +E, +S]:
  def start: ZIO[R & Scope, E, S]
