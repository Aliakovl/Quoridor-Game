package ru.quoridor.config

import zio._
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigProvider

final case class TokenKeys(privateKeyPath: String, publicKeyPath: String)

object TokenKeys {
  val layer: TaskLayer[TokenKeys] = ZLayer {
    TypesafeConfigProvider.fromResourcePath
      .nested("tokenKeys")
      .load(deriveConfig[TokenKeys])
  }
}
