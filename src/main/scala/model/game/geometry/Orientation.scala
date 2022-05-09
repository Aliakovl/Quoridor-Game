package model.game.geometry

import doobie.Meta
import doobie.postgres.implicits.pgEnumStringOpt
import model.game.geometry.Orientation._


sealed trait Orientation extends Opposite[Orientation] { self =>
  override def opposite: Orientation = self match {
    case Orientation.Horizontal => Vertical
    case Orientation.Vertical => Horizontal
  }
}

object Orientation {
  case object Horizontal extends Orientation
  case object Vertical extends Orientation

  implicit val orientationMeta: Meta[Orientation] = pgEnumStringOpt("orientation", fromEnum, toEnum)

  def toEnum(orientation: Orientation): String = orientation.toString.toLowerCase

  private def fromEnum(string: String): Option[Orientation] = {
    Option(string.toLowerCase).collect {
      case "horizontal" => Horizontal
      case "vertical" => Vertical
    }
  }
}
