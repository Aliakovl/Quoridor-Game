package dev.aliakovl.quoridor.codec

import cats.Show
import dev.aliakovl.quoridor.codec.string.given
import dev.aliakovl.quoridor.engine.game.geometry.Side
import dev.aliakovl.quoridor.engine.game.geometry.Side.*
import dev.aliakovl.utils.StringParser
import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema
import sttp.tapir.generic.auto.*

object json:
  given Encoder[Side] = Encoder.encodeString.contramap(Show[Side].show)
  given Decoder[Side] = Decoder.decodeString.emap(
    StringParser[Side].parse(_).toRight("Failed to parse String as Side")
  )
  given Schema[Side] = Schema.derivedSchema[Side]
