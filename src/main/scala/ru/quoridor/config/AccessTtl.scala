package ru.quoridor.config

import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class AccessTtl(ttl: Duration) extends AnyVal

object AccessTtl {
  val layer: TaskLayer[AccessTtl] = ZLayer {
    read {
      descriptor[AccessTtl].from(
        TypesafeConfigSource.fromResourcePath
          .at(PropertyTreePath.$("auth.ttl"))
      )
    }
  }
}
