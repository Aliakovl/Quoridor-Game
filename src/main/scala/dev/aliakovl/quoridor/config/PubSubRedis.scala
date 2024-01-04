package dev.aliakovl.quoridor.config

final case class PubSubRedis(
    host: String,
    port: Int,
    database: Int,
    password: String
)
