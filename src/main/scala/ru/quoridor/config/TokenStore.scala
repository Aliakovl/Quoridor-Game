package ru.quoridor.config

import zio._

final case class TokenStore(
    host: String,
    port: Int,
    databaseNumber: Int,
    password: String,
    ttl: Duration
)

object TokenStore {
  val live: RLayer[Configuration, TokenStore] = ZLayer(
    ZIO.serviceWith[Configuration](_.tokenStore)
  )
}
