package dev.aliakovl.quoridor.model.game.geometry

import dev.aliakovl.quoridor.engine.game.geometry.Orientation
import io.circe.*
import io.circe.generic.semiauto.*
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class WallPosition(orientation: Orientation, row: Int, column: Int)

object WallPosition:
  given Encoder[WallPosition] = deriveEncoder
  given Decoder[WallPosition] = deriveDecoder
  given Schema[WallPosition] = Schema.derivedSchema[WallPosition]
