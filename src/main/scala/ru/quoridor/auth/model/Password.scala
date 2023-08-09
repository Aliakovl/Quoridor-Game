package ru.quoridor.auth.model

import io.circe.{Decoder, Encoder}

final case class Password(value: String) extends AnyVal

object Password {
  implicit val encoder: Encoder[Password] = Encoder[String].contramap(_.value)
  implicit val decoder: Decoder[Password] = Decoder[String].map(Password(_))
}
