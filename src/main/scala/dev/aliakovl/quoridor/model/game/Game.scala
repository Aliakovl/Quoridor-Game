package dev.aliakovl.quoridor.model.game

import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.tagging.ID
import io.circe.*
import io.circe.generic.semiauto.*
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class Game(id: ID[Game], step: Int, state: State, winner: Option[User])

object Game:
  given Encoder[Game] = deriveEncoder
  given Decoder[Game] = deriveDecoder
  given Schema[Game] = Schema.derivedSchema[Game]
