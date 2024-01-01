package dev.aliakovl.quoridor.config

import zio.*

final case class Auth(ttl: Duration)

object Auth:
  val live: RLayer[Configuration, Auth] = ZLayer(
    ZIO.serviceWith[Configuration](_.auth)
  )
