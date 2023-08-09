package ru.quoridor.config

import zio._
import zio.config.typesafe._
import zio.config.magnolia._

final case class Configuration(
    auth: Auth,
    address: Address,
    tokenKeys: TokenKeys,
    tokenStore: TokenStore
)

object Configuration {
  val live: ULayer[Configuration] = ZLayer {
    TypesafeConfigProvider.fromResourcePath
      .load(deriveConfig[Configuration])
      .orDie
  }
}
