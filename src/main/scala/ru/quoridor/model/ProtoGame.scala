package ru.quoridor.model

import ru.quoridor.model.game.Game
import ru.utils.tagging.Id

final case class ProtoGame(id: Id[Game], players: ProtoPlayers)
