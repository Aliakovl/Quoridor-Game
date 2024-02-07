package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.quoridor.Pawn.*

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

object Pawn:
  given Conversion[(Side, Cell, Int), ActivePawn] = ActivePawn.apply
  given Conversion[(Side, Cell, Int), WaitingPawn] = WaitingPawn.apply
  given Conversion[(Side, Cell, Int), NonActivePawn] = NonActivePawn.apply
