package ru.quoridor.model

import io.circe.*
import io.circe.generic.semiauto.*
import sttp.tapir.generic.auto.*
import ru.quoridor.auth.model.Username
import ru.utils.tagging.ID
import sttp.tapir.Schema

case class User(id: ID[User], username: Username)

object User:
  given Encoder[User] = deriveEncoder
  given Decoder[User] = deriveDecoder
  given Schema[User] = Schema.derivedSchema[User]