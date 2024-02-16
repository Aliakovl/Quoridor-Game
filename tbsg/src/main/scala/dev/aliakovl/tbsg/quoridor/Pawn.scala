package dev.aliakovl.tbsg.quoridor

import cats.Order
import dev.aliakovl.tbsg.quoridor.Pawn.*

case class Pawn(edge: Side, position: Cell, walls: Int)

object Pawn:
  given Ordering[Pawn] = Ordering.by(_.edge)
  given Order[Pawn] = Order.fromOrdering
end Pawn
