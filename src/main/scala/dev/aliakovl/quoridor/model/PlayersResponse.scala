package dev.aliakovl.quoridor.model

import cats.data.NonEmptyList
import dev.aliakovl.quoridor.engine.game.*
import dev.aliakovl.quoridor.model.PlayerResponse.*
import dev.aliakovl.utils.tagging.ID
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class PlayersResponse(
    activePlayer: PlayerResponse,
    enemies: NonEmptyList[PlayerResponse]
) {
  lazy val toList: List[PlayerResponse] = activePlayer +: enemies.toList
}

object PlayersResponse:
  given Encoder[PlayersResponse] = deriveEncoder
  given Decoder[PlayersResponse] = deriveDecoder
  given Schema[PlayersResponse] = Schema.derivedSchema[PlayersResponse]

  def withUsername(players: Players)(
      users: Map[ID[User], User]
  ): PlayersResponse =
    PlayersResponse(
      activePlayer = PlayerResponse.withUsername(players.activePlayer)(
        users(players.activePlayer.id).username
      ),
      enemies = players.enemies.map(player =>
        PlayerResponse.withUsername(player)(users(player.id).username)
      )
    )
