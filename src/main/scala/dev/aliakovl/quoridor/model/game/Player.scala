package dev.aliakovl.quoridor.model.game

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.engine.game.geometry.{PawnPosition, Side}
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.tagging.ID
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
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
  given Ordering[Player] = Ordering.by[Player, Side](_.target)

  given Encoder[Player] = deriveEncoder
  given Decoder[Player] = deriveDecoder
  given Schema[Player] = Schema.derivedSchema[Player]
