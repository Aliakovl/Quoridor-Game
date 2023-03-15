package ru.quoridor.config

import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class TokenStore(
    host: String,
    port: Int,
    databaseNumber: Int,
    password: String,
    ttl: Duration
)

object TokenStore {
  val layer: TaskLayer[TokenStore] = ZLayer {
    read {
      descriptor[TokenStore].from(
        TypesafeConfigSource.fromResourcePath
          .at(PropertyTreePath.$("redis.tokenStore"))
      )
    }
  }
}
