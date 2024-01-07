package dev.aliakovl.utils.redis.config

import zio.*

final case class TokenStore(
    host: String,
    port: Int,
    database: Int,
    password: String,
    ttl: Duration
)
