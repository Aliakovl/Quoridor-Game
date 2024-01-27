package dev.aliakovl.quoridor.engine.game.geometry

enum Orientation:
  case Horizontal, Vertical

object Orientation:
  given Opposite[Orientation] with
    extension (value: Orientation)
      override def opposite: Orientation = value match
        case Horizontal => Vertical
        case Vertical   => Horizontal
