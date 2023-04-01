package ru.quoridor.model.game.geometry

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

  override def values: IndexedSeq[Orientation] = findValues
}
