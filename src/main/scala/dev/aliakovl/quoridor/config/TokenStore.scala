package dev.aliakovl.quoridor.config

import zio.*

final case class TokenStore(
    host: String,
    port: Int,
    database: Int,
    password: String,
    ttl: Duration
)

object TokenStore:
  val live: RLayer[Configuration, TokenStore] = ZLayer(
    ZIO.serviceWith[Configuration](_.tokenStore)
  )
