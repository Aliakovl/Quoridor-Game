package model.game.geometry

import doobie.Meta
import doobie.postgres.implicits.pgEnumStringOpt


sealed trait Orientation extends Opposite[Orientation]

object Orientation {
  case object Horizontal extends Orientation {
    override val opposite: Orientation = Vertical
  }

  case object Vertical extends Orientation {
    override val opposite: Orientation = Horizontal
  }

  implicit val orientationMeta: Meta[Orientation] = pgEnumStringOpt("orientation", fromEnum, toEnum)

  def toEnum(orientation: Orientation): String = {
    orientation match {
      case Horizontal => "horizontal"
      case Vertical => "vertical"
    }
  }

  private def fromEnum(string: String): Option[Orientation] = {
    Option(string).collect {
      case "horizontal" => Horizontal
      case "vertical" => Vertical
    }
  }
}
