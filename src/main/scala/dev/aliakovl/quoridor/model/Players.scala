package dev.aliakovl.quoridor.model

import cats.data.NonEmptyList
import dev.aliakovl.quoridor.engine.game
import dev.aliakovl.quoridor.model.Player.*
import dev.aliakovl.utils.tagging.ID
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

  def withUsername(players: game.Players)(users: Map[ID[User], User]): Players =
    Players(
      activePlayer = Player.withUsername(players.activePlayer)(
        users(players.activePlayer.id).username
      ),
      enemies = players.enemies.map(player =>
        Player.withUsername(player)(users(player.id).username)
      )
    )
