package ru.quoridor.storage.dto

import ru.quoridor.model.game.geometry.Orientation

case class WallPosition(
    gameId: GameId,
    step: Int,
    orient: Orientation,
    row: Int,
    column: Int
)
