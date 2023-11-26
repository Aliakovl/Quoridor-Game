package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.engine.Game
import dev.aliakovl.utils.tagging.ID

case class GamePreView(id: ID[Game], players: List[User], winner: Option[User])