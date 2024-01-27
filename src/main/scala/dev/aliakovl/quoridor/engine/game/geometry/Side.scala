package dev.aliakovl.quoridor.engine.game.geometry

import dev.aliakovl.quoridor.engine.game.geometry.Side.order

enum Side extends Opposite[Side] with Ordered[Side] { self =>
  case North extends Side
  case South extends Side
  case West extends Side
  case East extends Side

  override def opposite: Side = self match
    case North => South
    case South => North
    case West  => East
    case East  => West

  override def compare(that: Side): Int =
    Ordering.by[Side, Int](x => order(x)).compare(this, that)
}

object Side:
  private def order(side: Side): Int = side match
    case North => 0
    case East  => 1
    case South => 2
    case West  => 3
