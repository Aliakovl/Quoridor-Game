package ru.quoridor.model

import ru.quoridor.model.game.Game
import ru.utils.tagging.ID

final case class GamePreView(
    id: ID[Game],
    players: List[User],
    winner: Option[User]
)
