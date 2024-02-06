package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.engine.game.State
import dev.aliakovl.utils.tagging.ID

case class Game(
    id: ID[Game],
    step: Int,
    state: State,
    winner: Option[User]
)
