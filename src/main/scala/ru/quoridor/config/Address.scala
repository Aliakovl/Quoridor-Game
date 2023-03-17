package ru.quoridor.config

import zio._
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigProvider

final case class Address(host: String, port: Int)

object Address {
  val layer: TaskLayer[Address] = ZLayer {
    TypesafeConfigProvider.fromResourcePath
      .nested("address")
      .load(deriveConfig[Address])
  }
}
