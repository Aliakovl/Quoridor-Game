package dev.aliakovl.quoridor.dao.dto

case class GameState(
    gameId: GameId,
    step: Int,
    activePlayer: UserId
)
