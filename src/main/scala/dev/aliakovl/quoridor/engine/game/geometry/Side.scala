package dev.aliakovl.quoridor.engine.game.geometry

enum Side extends Ordered[Side]:
  case North, East, South, West

  override def compare(that: Side): Int =
    Ordering.by[Side, Int](_.ordinal).compare(this, that)

object Side:
  given Opposite[Side] with
    extension (value: Side)
      override def opposite: Side = value match
        case North => South
        case South => North
        case West  => East
        case East  => West
