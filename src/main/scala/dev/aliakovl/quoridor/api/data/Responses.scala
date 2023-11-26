package dev.aliakovl.quoridor.api.data

import cats.data.NonEmptyList
import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.engine.Game
import dev.aliakovl.quoridor.engine.geometry.{Orientation, Side}
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.tagging.ID

object Responses:
  case class GamePreViewResponse(
      id: ID[Game],
      players: List[UserResponse],
      winner: Option[UserResponse]
  )

  case class GameResponse(
      id: ID[Game],
      step: Int,
      state: StateResponse,
      winner: Option[UserResponse]
  )

  case class PawnPositionResponse(
      row: Int,
      column: Int
  )

  case class PlayerResponse(
      id: ID[User],
      username: Username,
      pawnPosition: PawnPositionResponse,
      wallsAmount: Int,
      target: Side
  )

  case class PlayersResponse(
      activePlayer: PlayerResponse,
      enemies: NonEmptyList[PlayerResponse]
  )

  case class ProtoGameResponse(
      id: ID[Game],
      players: ProtoPlayersResponse
  )

  case class ProtoPlayerResponse(
      id: ID[User],
      username: Username,
      target: Side
  )

  case class ProtoPlayersResponse(
      creator: ProtoPlayerResponse,
      guests: List[ProtoPlayerResponse]
  )

  case class StateResponse(
      players: PlayersResponse,
      walls: Set[WallPositionResponse]
  )

  case class UserResponse(
      id: ID[User],
      username: Username
  )

  case class WallPositionResponse(
      orientation: Orientation,
      row: Int,
      column: Int
  )
