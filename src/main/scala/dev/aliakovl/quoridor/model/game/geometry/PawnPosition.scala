package dev.aliakovl.quoridor.model.game.geometry

import io.circe.generic.semiauto.*
import io.circe.*
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class PawnPosition(row: Int, column: Int)

object PawnPosition:
  given Encoder[PawnPosition] = deriveEncoder
  given Decoder[PawnPosition] = deriveDecoder
  given Schema[PawnPosition] = Schema.derivedSchema[PawnPosition]
