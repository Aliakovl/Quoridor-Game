package dev.aliakovl.quoridor.config

import zio.*
import zio.config.typesafe.*
import zio.config.magnolia.*

case class Configuration(
    auth: Auth,
    address: Address,
    tokenKeys: TokenKeys,
    tokenStore: TokenStore,
    sslKeyStore: SSLKeyStore,
    pubSubRedis: PubSubRedis
)

object Configuration:
  val live: ULayer[Configuration] = ZLayer {
    TypesafeConfigProvider
      .fromResourcePath()
      .load(deriveConfig[Configuration])
      .orDie
  }