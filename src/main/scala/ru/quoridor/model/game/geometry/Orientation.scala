package ru.quoridor.model.game.geometry

import enumeratum.EnumEntry.Snakecase
import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

enum Orientation extends Opposite[Orientation] { self =>
  case Horizontal extends Orientation
  case Vertical extends Orientation

  override def opposite: Orientation = self match {
    case Horizontal => Vertical
    case Vertical   => Horizontal
  }

  def entryName: String = self match
    case Horizontal => "horizontal"
    case Vertical => "vertical"
}

object Orientation:
  def withName(name: String): Orientation = name match
    case "horizontal" => Horizontal
    case "vertical" => Vertical

  given Encoder[Orientation] = deriveEncoder
  given Decoder[Orientation] = deriveDecoder
  given Schema[Orientation] = Schema.derivedEnumeration[Orientation].defaultStringBased
