package ru.quoridor.app

final case class Address(host: String, port: Int)

final case class DB(driver: String, url: String, user: String, password: String)

final case class AppConfig(address: Address, DB: DB)
