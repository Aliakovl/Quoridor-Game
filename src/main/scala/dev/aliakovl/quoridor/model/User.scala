package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.utils.tagging.ID
import io.circe.*
import io.circe.generic.semiauto.*
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class User(id: ID[User], username: Username)

object User:
  given Encoder[User] = deriveEncoder
  given Decoder[User] = deriveDecoder
  given Schema[User] = Schema.derivedSchema[User]
