package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.engine.game.geometry.{PawnPosition, Side}
import dev.aliakovl.quoridor.engine.game.*
import dev.aliakovl.utils.tagging.ID
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class PlayerResponse(
    id: ID[User],
    username: Username,
    pawnPosition: PawnPosition,
    wallsAmount: Int,
    target: Side
)

object PlayerResponse:
  import dev.aliakovl.quoridor.codec.json.given

  given Encoder[PlayerResponse] = deriveEncoder
  given Decoder[PlayerResponse] = deriveDecoder
  given Schema[PlayerResponse] = Schema.derivedSchema[PlayerResponse]

  def fromPlayer(username: Username)(player: Player): PlayerResponse =
    player match
      case Player(id, pawnPosition, wallsAmount, target) =>
        PlayerResponse(id, username, pawnPosition, wallsAmount, target)
