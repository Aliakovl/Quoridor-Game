package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.engine.game.State
import dev.aliakovl.utils.tagging.ID
import io.circe.*
import io.circe.generic.semiauto.*
import sttp.tapir.Schema
import sttp.tapir.generic.auto.*

case class Game(id: ID[Game], step: Int, state: State, winner: Option[User])

object Game:
  import dev.aliakovl.quoridor.codec.json.given

  given Encoder[Game] = deriveEncoder
  given Decoder[Game] = deriveDecoder
  given Schema[Game] = Schema.derivedSchema[Game]
