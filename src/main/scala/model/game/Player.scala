package model.game

import model.User
import model.game.geometry.{PawnPosition, Side}
import utils.Typed.ID


case class Player(id: ID[User], login: String, pawnPosition: PawnPosition, wallsAmount: Int, target: Side)

object Player {
  implicit val ord: Ordering[Player] = Ordering.by[Player, Side](_.target)
}