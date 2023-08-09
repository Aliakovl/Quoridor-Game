package ru.quoridor.dao.dto

final case class PawnPosition(
    gameId: GameId,
    step: Int,
    userId: UserId,
    wallsAmount: Int,
    row: Int,
    column: Int
)
