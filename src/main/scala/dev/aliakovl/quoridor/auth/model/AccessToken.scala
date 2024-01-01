package dev.aliakovl.quoridor.auth.model

import io.circe.{Decoder, Encoder, HCursor, Json}

case class AccessToken(value: String) extends AnyVal

object AccessToken:
  given Encoder[AccessToken] = (a: AccessToken) =>
    Json.obj(
      "accessToken" -> Json.fromString(a.value)
    )

  given Decoder[AccessToken] = (c: HCursor) =>
    c.downField("accessToken").as[String].map(AccessToken(_))
