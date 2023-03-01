package ru.quoridor.model.game.geometry

import doobie.Meta
import doobie.postgres.implicits.pgEnumStringOpt
import enumeratum.EnumEntry.Snakecase
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait Orientation
    extends Opposite[Orientation]
    with EnumEntry
    with Snakecase { self =>
  import Orientation._

  override def opposite: Orientation = self match {
    case Horizontal => Vertical
    case Vertical   => Horizontal
  }
}

object Orientation extends Enum[Orientation] with CirceEnum[Orientation] {
  case object Horizontal extends Orientation
  case object Vertical extends Orientation

  implicit val orientationMeta: Meta[Orientation] =
    pgEnumStringOpt("orientation", fromEnum, toEnum)

  def toEnum(orientation: Orientation): String =
    orientation.toString.toLowerCase

  private def fromEnum(string: String): Option[Orientation] = {
    Option(string.toLowerCase).collect {
      case "horizontal" => Horizontal
      case "vertical"   => Vertical
    }
  }

  override def values: IndexedSeq[Orientation] = findValues
}
