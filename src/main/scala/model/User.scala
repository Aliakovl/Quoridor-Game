package model

import io.circe.{Decoder, Encoder, Json}
import sttp.tapir.Schema
import utils.Typed.ID
import utils.Typed.Implicits._


case class User(id: ID[User], login: String)

object User {

  implicit val jsonDecoder: Decoder[ID[User]] = Decoder.decodeUUID.map(_.typed[User])

  implicit val jsonEncoder: Encoder[ID[User]] = (a: ID[User]) => Json.fromString(a.unType.toString)

  implicit lazy val sid: Schema[ID[User]] = Schema.schemaForUUID.as
}