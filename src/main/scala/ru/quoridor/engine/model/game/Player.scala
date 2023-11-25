package ru.quoridor.engine.model.game

import ru.quoridor.auth.model.Username
import ru.quoridor.engine.model.User
import ru.quoridor.engine.model.game.geometry.{PawnPosition, Side}
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
