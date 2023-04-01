package ru.quoridor.auth.model

import io.circe.{Decoder, Encoder, HCursor, Json}

case class AccessToken(value: String) extends AnyVal

object AccessToken {
  implicit val encoder: Encoder[AccessToken] = (a: AccessToken) =>
    Json.obj(
      "accessToken" -> Json.fromString(a.value)
    )

  implicit val decoder: Decoder[AccessToken] = (c: HCursor) =>
    c.downField("accessToken").as[String].map(AccessToken(_))
}
