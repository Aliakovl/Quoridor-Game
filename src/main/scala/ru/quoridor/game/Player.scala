package ru.quoridor.game

import ru.quoridor.User
import ru.quoridor.game.geometry.{PawnPosition, Side}
import ru.utils.Typed.ID

case class Player(
    id: ID[User],
    login: String,
    pawnPosition: PawnPosition,
    wallsAmount: Int,
    target: Side
) {
  def toUser: User = User(id, login)
}

object Player {
  implicit val ord: Ordering[Player] = Ordering.by[Player, Side](_.target)
}
