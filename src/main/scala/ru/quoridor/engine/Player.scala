package ru.quoridor.engine

import ru.quoridor.auth.model.Username
import ru.quoridor.model.User
import ru.quoridor.engine.geometry.{PawnPosition, Side}
import ru.utils.tagging.ID

case class Player(
    id: ID[User],
    username: Username,
    pawnPosition: PawnPosition,
    wallsAmount: Int,
    target: Side
)

object Player:
  given Ordering[Player] = Ordering.by[Player, Side](_.target)
