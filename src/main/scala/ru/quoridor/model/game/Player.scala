package ru.quoridor.model.game

import ru.quoridor.model.User
import ru.quoridor.model.game.geometry.{PawnPosition, Side}
import ru.utils.Tagged.ID

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
