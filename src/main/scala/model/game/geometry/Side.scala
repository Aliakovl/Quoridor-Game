package model.game.geometry

import doobie.Meta
import doobie.postgres.implicits.pgEnumStringOpt


sealed trait Side extends Opposite[Side]

object Side {
  object North extends Side {
    override val opposite: Side = South
  }

  object South extends Side {
    override val opposite: Side = North
  }

  object West extends Side {
    override val opposite: Side = East
  }

  object East extends Side {
    override val opposite: Side = West
  }

  val allSides: Seq[Side] = Seq(North, South, West, East)

  implicit val sideMeta: Meta[Side] = pgEnumStringOpt("side", Side.fromEnum, Side.toEnum)

  def toEnum(side: Side): String = {
    side match {
      case North => "north"
      case South => "south"
      case West => "west"
      case East => "east"
    }
  }

  private def fromEnum(string: String): Option[Side] = {
    Option(string).collect {
      case "north" => North
      case "south" => South
      case "west" => West
      case "east" => East
    }
  }
}