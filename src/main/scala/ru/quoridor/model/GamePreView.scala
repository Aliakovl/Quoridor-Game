package ru.quoridor.model

import java.util.UUID

case class GamePreView(
    gameId: UUID,
    players: List[User],
    winner: Option[User]
)
