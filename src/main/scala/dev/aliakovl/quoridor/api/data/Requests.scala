package dev.aliakovl.quoridor.api.data

import dev.aliakovl.quoridor.engine.geometry.Orientation

object Requests:
  case class PawnMoveRequest(pawnPosition: PawnPositionRequest)

  case class PawnPositionRequest(
      row: Int,
      column: Int
  )

  case class PlaceWallRequest(wallPosition: WallPositionRequest)

  case class WallPositionRequest(
      orientation: Orientation,
      row: Int,
      column: Int
  )
