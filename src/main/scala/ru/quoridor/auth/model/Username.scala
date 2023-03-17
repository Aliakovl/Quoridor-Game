package ru.quoridor.auth.model

import io.circe.{Decoder, Encoder}

case class Username(value: String) extends AnyVal

object Username {
  implicit val encoder: Encoder[Username] = Encoder[String].contramap(_.value)
  implicit val decoder: Decoder[Username] = Decoder[String].map(Username(_))
}
