package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.engine.game.State
import dev.aliakovl.quoridor.engine.game.geometry.WallPosition
import dev.aliakovl.utils.tagging.ID
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class StateResponse(players: PlayersResponse, walls: Set[WallPosition])

object StateResponse:
  def fromState(users: Map[ID[User], User])(state: State): StateResponse =
    state match
      case State(players, walls) =>
        StateResponse(PlayersResponse.fromPlayers(users)(players), walls)

  import dev.aliakovl.quoridor.codec.json.given

  given Encoder[StateResponse] = deriveEncoder
  given Decoder[StateResponse] = deriveDecoder
  given Schema[StateResponse] = Schema.derivedSchema[StateResponse]
