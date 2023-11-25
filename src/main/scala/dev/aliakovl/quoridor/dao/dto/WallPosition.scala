package dev.aliakovl.quoridor.dao.dto

import dev.aliakovl.quoridor.engine.geometry.Orientation

case class WallPosition(
    gameId: GameId,
    step: Int,
    orient: Orientation,
    row: Int,
    column: Int
)
