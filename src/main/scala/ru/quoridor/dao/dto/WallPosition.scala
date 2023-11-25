package ru.quoridor.dao.dto

import ru.quoridor.engine.geometry.Orientation

case class WallPosition(
    gameId: GameId,
    step: Int,
    orient: Orientation,
    row: Int,
    column: Int
)
