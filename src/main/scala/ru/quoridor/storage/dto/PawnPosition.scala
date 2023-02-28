package ru.quoridor.storage.dto

case class PawnPosition(
    gameId: GameId,
    step: Int,
    userId: UserId,
    wallsAmount: Int,
    row: Int,
    column: Int
)
