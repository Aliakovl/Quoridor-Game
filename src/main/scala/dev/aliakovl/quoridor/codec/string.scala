package dev.aliakovl.quoridor.codec

import cats.Show
import dev.aliakovl.quoridor.engine.game.geometry.*
import dev.aliakovl.quoridor.engine.game.geometry.Orientation.*
import dev.aliakovl.quoridor.engine.game.geometry.Side.*
import dev.aliakovl.utils.StringParser

object string:
  given Show[Side] with
    override def show(side: Side): String = side match
      case North => "north"
      case South => "south"
      case West  => "west"
      case East  => "east"

  given StringParser[Side] with
    override def parse(from: String): Option[Side] = from.toLowerCase match
      case "north" => Some(North)
      case "south" => Some(South)
      case "west"  => Some(West)
      case "east"  => Some(East)
      case _       => None

  given Show[Orientation] with
    override def show(orientation: Orientation): String = orientation match
      case Horizontal => "horizontal"
      case Vertical   => "vertical"

  given StringParser[Orientation] with
    override def parse(from: String): Option[Orientation] =
      from.toLowerCase match
        case "horizontal" => Some(Horizontal)
        case "vertical"   => Some(Vertical)
        case _            => None
