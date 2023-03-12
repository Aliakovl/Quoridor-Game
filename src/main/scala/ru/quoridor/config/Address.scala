package ru.quoridor.config

import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

final case class Address(host: String, port: Int)

object Address {
  val layer: TaskLayer[Address] = ZLayer {
    read {
      descriptor[Address].from(
        TypesafeConfigSource.fromResourcePath
          .at(PropertyTreePath.$("address"))
      )
    }
  }
}
