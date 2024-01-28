package dev.aliakovl.quoridor.services.model

import dev.aliakovl.quoridor.engine.game.State
import dev.aliakovl.quoridor.model.*
import dev.aliakovl.utils.tagging.ID
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class GameResponse(
    id: ID[Game],
    step: Int,
    state: StateResponse,
    winner: Option[User]
)

object GameResponse:
  def fromGame(users: Map[ID[User], User])(game: Game): GameResponse =
    game match
      case Game(id, step, State(players, walls), winner) =>
        GameResponse(
          id,
          step,
          StateResponse(PlayersResponse.fromPlayers(users)(players), walls),
          winner
        )

  given Encoder[GameResponse] = deriveEncoder
  given Decoder[GameResponse] = deriveDecoder
  given Schema[GameResponse] = Schema.derivedSchema[GameResponse]
