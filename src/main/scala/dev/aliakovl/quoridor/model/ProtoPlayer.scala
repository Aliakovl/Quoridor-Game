package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.engine.game.geometry.Side
import dev.aliakovl.utils.tagging.ID
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class ProtoPlayer(id: ID[User], username: Username, target: Side)

object ProtoPlayer:
  given Schema[ProtoPlayer] = Schema.derivedSchema[ProtoPlayer]
