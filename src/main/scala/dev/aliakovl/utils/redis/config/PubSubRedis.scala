package dev.aliakovl.utils.redis.config

final case class PubSubRedis(
    host: String,
    port: Int,
    database: Int,
    password: String
)
