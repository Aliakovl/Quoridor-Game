package dev.aliakovl.quoridor.engine

import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.tagging.ID

case class Game(id: ID[Game], step: Int, state: State, winner: Option[User])
