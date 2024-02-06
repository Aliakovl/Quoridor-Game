package dev.aliakovl.quoridor.model

import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class ProtoPlayers(creator: ProtoPlayer, guests: List[ProtoPlayer])

object ProtoPlayers:
  given Schema[ProtoPlayers] = Schema.derivedSchema[ProtoPlayers]
