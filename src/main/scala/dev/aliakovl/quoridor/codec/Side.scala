package dev.aliakovl.quoridor.codec

import dev.aliakovl.quoridor.engine.geometry.Side
import dev.aliakovl.quoridor.engine.geometry.Side.*

object Side:
  def withName(name: String): Side = name match
    case "north" => North
    case "south" => South
    case "west"  => West
    case "east"  => East

  extension (side: Side)
    def entryName: String = side match
      case North => "north"
      case South => "south"
      case West  => "west"
      case East  => "east"
