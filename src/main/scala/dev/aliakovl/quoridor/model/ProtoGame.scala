package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.engine.Game
import dev.aliakovl.utils.tagging.ID

case class ProtoGame(id: ID[Game], players: ProtoPlayers)
