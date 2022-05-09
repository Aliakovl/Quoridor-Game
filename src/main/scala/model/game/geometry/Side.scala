package model.game.geometry

import doobie.Meta
import doobie.postgres.implicits.pgEnumStringOpt
import model.game.geometry.Side._


sealed trait Side extends Opposite[Side] { self =>
  override def opposite: Side = self match {
    case North => South
    case South => North
    case West => East
    case East => West
  }
}

object Side {
  case object North extends Side
  case object South extends Side
  case object West extends Side
  case object East extends Side

  val allSides: Seq[Side] = Seq(North, South, West, East)

  implicit val sideMeta: Meta[Side] = pgEnumStringOpt("side", Side.fromEnum, Side.toEnum)

  def toEnum(side: Side): String = side.toString.toLowerCase

  private def fromEnum(string: String): Option[Side] = {
    Option(string.toLowerCase).collect {
      case "north" => North
      case "south" => South
      case "west" => West
      case "east" => East
    }
  }
}