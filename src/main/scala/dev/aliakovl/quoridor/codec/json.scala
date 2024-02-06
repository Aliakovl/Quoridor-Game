package dev.aliakovl.quoridor.codec

import cats.Show
import dev.aliakovl.quoridor.codec.string.given
import dev.aliakovl.quoridor.engine.game.geometry.*
import dev.aliakovl.quoridor.engine.game.geometry.Side.*
import dev.aliakovl.utils.StringParser
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema
import sttp.tapir.generic.auto.*

object json:
  given Encoder[Side] = Encoder.encodeString.contramap(Show[Side].show)
  given Decoder[Side] = Decoder.decodeString.emap(
    StringParser[Side].parse(_).toRight("Failed to parse String as Side")
  )
  given Schema[Side] = Schema.derivedSchema[Side]

  given Encoder[Orientation] =
    Encoder.encodeString.contramap(Show[Orientation].show)
  given Decoder[Orientation] = Decoder.decodeString.emap(
    StringParser[Orientation]
      .parse(_)
      .toRight("Failed to parse String as Orientation")
  )
  given Schema[Orientation] = Schema.derivedSchema[Orientation]

  given Encoder[PawnPosition] = deriveEncoder
  given Decoder[PawnPosition] = deriveDecoder
  given Schema[PawnPosition] = Schema.derivedSchema[PawnPosition]

  given Encoder[WallPosition] = deriveEncoder
  given Decoder[WallPosition] = deriveDecoder
  given Schema[WallPosition] = Schema.derivedSchema[WallPosition]
