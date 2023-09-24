package ru.quoridor.model

import ru.quoridor.auth.model.Username
import ru.quoridor.model.game.geometry.Side
import ru.utils.tagging.ID
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class ProtoPlayer(id: ID[User], username: Username, target: Side)

object ProtoPlayer:
  given Schema[ProtoPlayer] = Schema.derivedSchema[ProtoPlayer]
