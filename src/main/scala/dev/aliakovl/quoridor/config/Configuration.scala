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

  val auth: ULayer[Auth] = live.project(_.auth)
  val address: ULayer[Address] = live.project(_.address)
  val tokenKeys: ULayer[TokenKeys] = live.project(_.tokenKeys)
  val tokenStore: ULayer[TokenStore] = live.project(_.tokenStore)
  val sslKeyStore: ULayer[SSLKeyStore] = live.project(_.sslKeyStore)
  val pubSubRedis: ULayer[PubSubRedis] = live.project(_.pubSubRedis)
