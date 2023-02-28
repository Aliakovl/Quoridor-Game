package ru.quoridor.storage.dto

case class GameState(
    gameId: GameId,
    step: Int,
    activePlayer: UserId
)
