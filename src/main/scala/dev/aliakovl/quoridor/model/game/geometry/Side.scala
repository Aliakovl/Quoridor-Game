package dev.aliakovl.quoridor.model.game.geometry

import dev.aliakovl.quoridor.engine.game.geometry.Opposite
import dev.aliakovl.quoridor.model.game.geometry.Side.order
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

import scala.util.Try

enum Side extends Opposite[Side] with Ordered[Side] { self =>

  case North extends Side
  case South extends Side
  case West extends Side
  case East extends Side

  override def opposite: Side = self match
    case North => South
    case South => North
    case West  => East
    case East  => West

  override def compare(that: Side): Int =
    Ordering.by[Side, Int](x => order(x)).compare(this, that)

  def entryName: String = self match
    case North => "north"
    case South => "south"
    case West  => "west"
    case East  => "east"
}

object Side:
  private def order(side: Side): Int = side match
    case North => 0
    case East  => 1
    case South => 2
    case West  => 3

  def withName(name: String): Side = name match
    case "north" => North
    case "south" => South
    case "west"  => West
    case "east"  => East

  given Encoder[Side] = Encoder.encodeString.contramap(_.entryName)
  given Decoder[Side] = Decoder.decodeString.emapTry { name =>
    Try(withName(name))
  }
  given Schema[Side] = Schema.derivedSchema[Side]
