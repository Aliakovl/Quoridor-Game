package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.quoridor.Pawn.*

import scala.compiletime.erasedValue

enum Pawn(val edge: Side, val position: Cell, val walls: Int):
  case ActivePawn(
      override val edge: Side,
      override val position: Cell,
      override val walls: Int
  ) extends Pawn(edge, position, walls)
  case WaitingPawn(
      override val edge: Side,
      override val position: Cell,
      override val walls: Int
  ) extends Pawn(edge, position, walls)
  case NonActivePawn(
      override val edge: Side,
      override val position: Cell,
      override val walls: Int
  ) extends Pawn(edge, position, walls)
end Pawn

object Pawn:
  inline transparent def apply[T <: Pawn](
      edge: Side,
      position: Cell,
      walls: Int
  ): Pawn = {
    inline erasedValue[T] match
      case _: Pawn.ActivePawn =>
        Pawn.ActivePawn(edge, position, walls)
      case _: Pawn.WaitingPawn =>
        Pawn.WaitingPawn(edge, position, walls)
      case _: Pawn.NonActivePawn =>
        Pawn.NonActivePawn(edge, position, walls)
  }
end Pawn
