package dev.aliakovl.quoridor.engine.game.geometry

enum Side:
  case South, West, North, East

object Side:
  given Ordering[Side] = Ordering.by[Side, Int](_.ordinal)

  given Opposite[Side] with
    extension (value: Side)
      override def opposite: Side = value match
        case North => South
        case South => North
        case West  => East
        case East  => West
