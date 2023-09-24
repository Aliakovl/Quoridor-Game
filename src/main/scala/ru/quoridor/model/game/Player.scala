package ru.quoridor.model.game

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import ru.quoridor.auth.model.Username
import ru.quoridor.model.User
import ru.quoridor.model.game.geometry.{PawnPosition, Side}
import ru.utils.tagging.ID
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
