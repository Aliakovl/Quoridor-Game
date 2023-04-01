package ru.quoridor.dao.dto

import ru.quoridor.model.game.geometry.Orientation

case class WallPosition(
    gameId: GameId,
    step: Int,
    orient: Orientation,
    row: Int,
    column: Int
)
