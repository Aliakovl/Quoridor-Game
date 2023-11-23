package ru.quoridor.codec.circe

import ru.quoridor.codec
import ru.quoridor.codec.Orientation.*
import ru.quoridor.model.game.geometry.Orientation
import io.circe.{Decoder, Encoder}

import scala.util.Try

object Orientation:
  given Encoder[Orientation] =
    Encoder.encodeString.contramap(_.entryName)

  given Decoder[Orientation] = Decoder.decodeString.emapTry { name =>
    Try(codec.Orientation.withName(name))
  }
