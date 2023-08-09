package ru.quoridor.model.game

import ru.quoridor.model.User
import ru.utils.tagging.ID

final case class Game(
    id: ID[Game],
    step: Int,
    state: State,
    winner: Option[User]
)
