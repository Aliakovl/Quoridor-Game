package ru.quoridor.config

import zio._
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigProvider

final case class AccessTtl(ttl: Duration)

object AccessTtl {
  val layer: TaskLayer[AccessTtl] = ZLayer {
    TypesafeConfigProvider.fromResourcePath
      .nested("auth")
      .load(deriveConfig[AccessTtl])
  }
}
