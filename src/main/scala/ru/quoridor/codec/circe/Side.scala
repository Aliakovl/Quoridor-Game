package ru.quoridor.codec.circe

import io.circe.*
import ru.quoridor.codec.Side.*
import ru.quoridor.engine.model.game.geometry.Side

import scala.util.Try

object Side:
  given Encoder[Side] = Encoder.encodeString.contramap(_.entryName)
  given Decoder[Side] = Decoder.decodeString.emapTry { name =>
    Try(withName(name))
  }
