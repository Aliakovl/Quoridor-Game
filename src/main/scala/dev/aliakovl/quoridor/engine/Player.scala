package dev.aliakovl.quoridor.engine

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.quoridor.engine.geometry.{PawnPosition, Side}
import dev.aliakovl.utils.tagging.ID

case class Player(
    id: ID[User],
    username: Username,
    pawnPosition: PawnPosition,
    wallsAmount: Int,
    target: Side
)

object Player:
  given Ordering[Player] = Ordering.by[Player, Side](_.target)
