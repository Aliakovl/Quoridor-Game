package dev.aliakovl.quoridor.model.game

import dev.aliakovl.utils.Shifting
import cats.data.NonEmptyList
import io.circe.*
import io.circe.generic.semiauto.*
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class Players(activePlayer: Player, enemies: NonEmptyList[Player]) {
  lazy val toList: List[Player] = activePlayer +: enemies.toList

  def shift: Players = {
    Shifting[Player](activePlayer, enemies).shift match {
      case Shifting(p, pp) => Players(p, pp)
    }
  }
}

object Players:
  given Encoder[Players] = deriveEncoder
  given Decoder[Players] = deriveDecoder
  given Schema[Players] = Schema.derivedSchema[Players]
