package dev.aliakovl.quoridor.engine.game

import dev.aliakovl.quoridor.engine.game.geometry.{PawnPosition, Side}
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.tagging.ID

case class Player(
    id: ID[User],
    pawnPosition: PawnPosition,
    wallsAmount: Int,
    target: Side
)

object Player:
  given Ordering[Player] = Ordering.by[Player, Side](_.target)
