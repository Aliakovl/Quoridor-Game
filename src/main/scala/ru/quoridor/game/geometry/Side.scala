package ru.quoridor.game.geometry

import doobie.Meta
import doobie.postgres.implicits.pgEnumStringOpt
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait Side extends Opposite[Side] with Ordered[Side] with EnumEntry {
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

  implicit val sideMeta: Meta[Side] =
    pgEnumStringOpt("side", Side.fromEnum, Side.toEnum)

  def toEnum(side: Side): String = side.toString.toLowerCase

  private def fromEnum(string: String): Option[Side] = {
    Option(string.toLowerCase).collect {
      case "north" => North
      case "south" => South
      case "west"  => West
      case "east"  => East
    }
  }

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
