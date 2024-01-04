package dev.aliakovl.quoridor.config

import zio.*

final case class TokenStore(
    host: String,
    port: Int,
    database: Int,
    password: String,
    ttl: Duration
)
