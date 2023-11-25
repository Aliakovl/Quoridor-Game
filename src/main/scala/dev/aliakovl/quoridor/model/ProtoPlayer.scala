package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.engine.geometry.Side
import dev.aliakovl.utils.tagging.ID

case class ProtoPlayer(id: ID[User], username: Username, target: Side)
