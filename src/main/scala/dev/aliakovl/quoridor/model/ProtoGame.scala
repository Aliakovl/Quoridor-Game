package dev.aliakovl.quoridor.model

import dev.aliakovl.utils.tagging.ID
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class ProtoGame(id: ID[Game], players: ProtoPlayers)

object ProtoGame:
  given Schema[ProtoGame] = Schema.derivedSchema[ProtoGame]
