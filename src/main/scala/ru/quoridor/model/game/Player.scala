package ru.quoridor.model.game

import ru.quoridor.model.User
import ru.quoridor.model.game.geometry.{PawnPosition, Side}
import ru.utils.Typed.ID

case class Player(
    userId: ID[User],
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
