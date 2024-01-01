package dev.aliakovl.quoridor.auth.model

import io.circe.*
import sttp.tapir.*

case class Username(value: String) extends AnyVal

object Username:
  given Encoder[Username] = Encoder[String].contramap(_.value)
  given Decoder[Username] = Decoder[String].map(Username(_))
  given Schema[Username] = Schema(SchemaType.SString())
