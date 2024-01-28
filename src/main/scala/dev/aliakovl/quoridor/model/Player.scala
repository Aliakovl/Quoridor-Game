package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.engine.game.geometry.{PawnPosition, Side}
import dev.aliakovl.quoridor.engine.game
import dev.aliakovl.utils.tagging.ID
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class Player(
    id: ID[User],
    username: Username,
    pawnPosition: PawnPosition,
    wallsAmount: Int,
    target: Side
)

object Player:
  import dev.aliakovl.quoridor.codec.json.given

  given Encoder[Player] = deriveEncoder
  given Decoder[Player] = deriveDecoder
  given Schema[Player] = Schema.derivedSchema[Player]

  def withUsername(player: game.Player)(username: Username): Player =
    player match
      case game.Player(id, pawnPosition, wallsAmount, target) =>
        Player(id, username, pawnPosition, wallsAmount, target)
