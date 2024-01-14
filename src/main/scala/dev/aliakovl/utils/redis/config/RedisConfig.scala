package dev.aliakovl.utils.redis.config

import zio.Duration

final case class RedisConfig(
    host: String,
    port: Int,
    database: Int,
    password: String,
    ttl: Option[Duration] = None
)
