package ru.quoridor.model

import ru.quoridor.model.game.Game
import ru.utils.tagging.ID
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class ProtoGame(id: ID[Game], players: ProtoPlayers)

object ProtoGame:
  given Schema[ProtoGame] = Schema.derivedSchema[ProtoGame]
