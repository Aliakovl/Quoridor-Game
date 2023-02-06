package ru.quoridor.model.game

import ru.quoridor.model.User
import ru.quoridor.model.game.geometry.{PawnPosition, Side}

import java.util.UUID

case class Player(
    userId: UUID,
    login: String,
    pawnPosition: PawnPosition,
    wallsAmount: Int,
    target: Side
) {
  def toUser: User = User(userId, login)
}

object Player {
  implicit val ord: Ordering[Player] = Ordering.by[Player, Side](_.target)
}
