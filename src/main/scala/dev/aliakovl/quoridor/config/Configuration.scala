package dev.aliakovl.quoridor.config

import dev.aliakovl.utils.redis.config.RedisConfig
import zio.*
import zio.config.typesafe.*
import zio.config.magnolia.*

case class Configuration(
    auth: Auth,
    address: Address,
    tokenKeys: TokenKeys,
    tokenStore: RedisConfig,
    sslKeyStore: SSLKeyStore,
    pubSubRedis: RedisConfig
)

object Configuration:
  val live: ULayer[Configuration] = ZLayer {
    TypesafeConfigProvider
      .fromResourcePath()
      .load(deriveConfig[Configuration])
      .orDie
  }

  val auth: ULayer[Auth] = live.project(_.auth)
  val address: ULayer[Address] = live.project(_.address)
  val tokenKeys: ULayer[TokenKeys] = live.project(_.tokenKeys)
  val tokenStore: ULayer[RedisConfig] = live.project(_.tokenStore)
  val sslKeyStore: ULayer[SSLKeyStore] = live.project(_.sslKeyStore)
  val pubSubRedis: ULayer[RedisConfig] = live.project(_.pubSubRedis)
