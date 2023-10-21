package ru.quoridor.config

import zio.*

final case class SSLKeyStore(path: String, password: String)

object SSLKeyStore {
  val live: RLayer[Configuration, SSLKeyStore] = ZLayer(
    ZIO.serviceWith[Configuration](_.sslKeyStore)
  )
}
