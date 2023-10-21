package ru.quoridor.config

import zio.*
import zio.config.typesafe.*
import zio.config.magnolia.*

case class Configuration(
    auth: Auth,
    address: Address,
    tokenKeys: TokenKeys,
    tokenStore: TokenStore,
    sslKeyStore: SSLKeyStore
)

object Configuration {
  val live: ULayer[Configuration] = ZLayer {
    TypesafeConfigProvider
      .fromResourcePath()
      .load(deriveConfig[Configuration])
      .orDie
  }
}
