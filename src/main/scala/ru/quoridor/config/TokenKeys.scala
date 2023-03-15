package ru.quoridor.config

import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class TokenKeys(privateKeyPath: String, publicKeyPath: String)

object TokenKeys {
  val layer: TaskLayer[TokenKeys] = ZLayer {
    read {
      descriptor[TokenKeys].from(
        TypesafeConfigSource.fromResourcePath
          .at(PropertyTreePath.$("tokenKeys"))
      )
    }
  }
}
