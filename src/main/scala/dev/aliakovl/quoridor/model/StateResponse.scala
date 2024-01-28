package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.engine.game.geometry.WallPosition
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class StateResponse(players: PlayersResponse, walls: Set[WallPosition])

object StateResponse:
  import dev.aliakovl.quoridor.codec.json.given

  given Encoder[StateResponse] = deriveEncoder
  given Decoder[StateResponse] = deriveDecoder
  given Schema[StateResponse] = Schema.derivedSchema[StateResponse]
