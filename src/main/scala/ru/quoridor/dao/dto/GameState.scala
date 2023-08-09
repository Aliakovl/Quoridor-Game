package ru.quoridor.dao.dto

final case class GameState(
    gameId: GameId,
    step: Int,
    activePlayer: UserId
)
