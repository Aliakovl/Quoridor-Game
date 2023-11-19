package ru.quoridor.auth.model

import zio.{UIO, ZIO}

import java.security.SecureRandom
import java.util.Base64

case class RefreshToken(value: String) extends AnyVal

object RefreshToken:
  private val random = new SecureRandom
  private val encoder = Base64.getEncoder
  private val length = 64

  val generate: UIO[RefreshToken] = ZIO
    .succeed {
      val bytes = new Array[Byte](length)
      random.nextBytes(bytes)
      encoder.encodeToString(bytes)
    }
    .map(RefreshToken(_))
