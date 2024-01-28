package dev.aliakovl.quoridor.model

import cats.data.NonEmptyList
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class Players(activePlayer: Player, enemies: NonEmptyList[Player]) {
  lazy val toList: List[Player] = activePlayer +: enemies.toList
}

object Players:
  given Encoder[Players] = deriveEncoder
  given Decoder[Players] = deriveDecoder
  given Schema[Players] = Schema.derivedSchema[Players]
