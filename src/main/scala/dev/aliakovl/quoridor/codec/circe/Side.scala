package dev.aliakovl.quoridor.codec.circe

import io.circe.*
import dev.aliakovl.quoridor.codec.Side.*
import dev.aliakovl.quoridor.engine.geometry.Side

import scala.util.Try

object Side:
  given Encoder[Side] = Encoder.encodeString.contramap(_.entryName)
  given Decoder[Side] = Decoder.decodeString.emapTry { name =>
    Try(withName(name))
  }