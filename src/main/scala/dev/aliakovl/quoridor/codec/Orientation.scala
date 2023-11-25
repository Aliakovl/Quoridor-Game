package dev.aliakovl.quoridor.codec

import dev.aliakovl.quoridor.engine.geometry.Orientation
import dev.aliakovl.quoridor.engine.geometry.Orientation.*

object Orientation:
  def withName(name: String): Orientation = name match
    case "horizontal" => Horizontal
    case "vertical"   => Vertical

  extension (orientation: Orientation)
    def entryName: String = orientation match
      case Horizontal => "horizontal"
      case Vertical   => "vertical"
