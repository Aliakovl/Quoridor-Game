package ru.quoridor.app

final case class Address(host: String, port: Int)

final case class AppConfig(address: Address)
