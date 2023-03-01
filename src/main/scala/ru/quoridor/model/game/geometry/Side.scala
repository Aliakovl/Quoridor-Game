package ru.quoridor.model.game.geometry

import enumeratum.EnumEntry.Snakecase
import enumeratum.{CirceEnum, Enum}

sealed trait Side extends Opposite[Side] with Ordered[Side] with Snakecase {
  self =>
  import Side._

  override def opposite: Side = self match {
    case North => South
    case South => North
    case West  => East
    case East  => West
  }

  override def compare(that: Side): Int =
    Ordering.by[Side, Int](x => order(x)).compare(this, that)
}

object Side extends Enum[Side] with CirceEnum[Side] {
  case object North extends Side
  case object South extends Side
  case object West extends Side
  case object East extends Side

  val allSides: Seq[Side] = Seq(North, South, West, East)

  private def order(side: Side): Int = {
    side match {
      case North => 0
      case East  => 1
      case South => 2
      case West  => 3
    }
  }

  override def values: IndexedSeq[Side] = findValues
}
