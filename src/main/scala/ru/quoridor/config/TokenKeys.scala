package ru.quoridor.config

import zio._

final case class TokenKeys(privateKeyPath: String, publicKeyPath: String)

object TokenKeys {
  val live: RLayer[Configuration, TokenKeys] = ZLayer(
    ZIO.serviceWith[Configuration](_.tokenKeys)
  )
}
