package dev.aliakovl.quoridor.model.game.geometry

import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

import scala.util.Try

enum Orientation extends Opposite[Orientation] { self =>
  case Horizontal extends Orientation
  case Vertical extends Orientation

  override def opposite: Orientation = self match {
    case Orientation.Horizontal => Vertical
    case Orientation.Vertical   => Horizontal
  }

  def entryName: String = self match
    case Orientation.Horizontal => "horizontal"
    case Orientation.Vertical   => "vertical"
}

object Orientation:
  def withName(name: String): Orientation = name match
    case "horizontal" => Horizontal
    case "vertical"   => Vertical

  given Encoder[Orientation] = Encoder.encodeString.contramap(_.entryName)
  given Decoder[Orientation] = Decoder.decodeString.emapTry { name =>
    Try(withName(name))
  }
  given Schema[Orientation] = Schema.derivedSchema[Orientation]
