package dev.aliakovl.quoridor.codec.circe

import dev.aliakovl.quoridor.codec
import dev.aliakovl.quoridor.codec.Orientation.*
import dev.aliakovl.quoridor.engine.geometry.Orientation
import io.circe.{Decoder, Encoder}

import scala.util.Try

object Orientation:
  given Encoder[Orientation] =
    Encoder.encodeString.contramap(_.entryName)

  given Decoder[Orientation] = Decoder.decodeString.emapTry { name =>
    Try(codec.Orientation.withName(name))
  }
