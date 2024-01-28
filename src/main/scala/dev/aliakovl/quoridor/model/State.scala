package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.engine.game.geometry.WallPosition
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class State(players: Players, walls: Set[WallPosition])

object State:
  import dev.aliakovl.quoridor.codec.json.given

  given Encoder[State] = deriveEncoder
  given Decoder[State] = deriveDecoder
  given Schema[State] = Schema.derivedSchema[State]
