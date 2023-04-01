package ru.quoridor.config

import zio._

final case class Auth(ttl: Duration)

object Auth {
  val live: RLayer[Configuration, Auth] = ZLayer(
    ZIO.serviceWith[Configuration](_.auth)
  )
}
