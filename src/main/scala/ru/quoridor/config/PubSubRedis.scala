package ru.quoridor.config

import zio.*

final case class PubSubRedis(
    host: String,
    port: Int,
    database: Int,
    password: String
)

object PubSubRedis:
  val live: RLayer[Configuration, PubSubRedis] = ZLayer(
    ZIO.serviceWith[Configuration](_.pubSubRedis)
  )
