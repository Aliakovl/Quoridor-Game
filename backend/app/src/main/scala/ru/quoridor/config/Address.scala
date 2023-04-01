package ru.quoridor.config

import zio._

final case class Address(host: String, port: Int)

object Address {
  val live: RLayer[Configuration, Address] = ZLayer(
    ZIO.serviceWith[Configuration](_.address)
  )
}
