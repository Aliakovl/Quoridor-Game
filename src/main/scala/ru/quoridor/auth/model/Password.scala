package ru.quoridor.auth.model

import io.circe.{Decoder, Encoder}
import sttp.tapir.{Schema, SchemaType}

case class Password(value: String) extends AnyVal

object Password:
  given Encoder[Password] = Encoder[String].contramap(_.value)
  given Decoder[Password] = Decoder[String].map(Password(_))
  given Schema[Password] = Schema(SchemaType.SString())
