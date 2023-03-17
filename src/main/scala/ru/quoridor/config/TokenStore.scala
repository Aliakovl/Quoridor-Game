package ru.quoridor.config

import zio._
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigProvider

final case class TokenStore(
    host: String,
    port: Int,
    databaseNumber: Int,
    password: String,
    ttl: Duration
)

object TokenStore {
  val layer: TaskLayer[TokenStore] = ZLayer {
    TypesafeConfigProvider.fromResourcePath
      .nested("tokenStore")
      .nested("redis")
      .load(deriveConfig[TokenStore])
  }
}
