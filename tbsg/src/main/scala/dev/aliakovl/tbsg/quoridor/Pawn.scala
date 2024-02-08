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
