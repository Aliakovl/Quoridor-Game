package ru.quoridor.model

import ru.quoridor.model.game.Game
import ru.utils.tagging.ID

final case class ProtoGame(id: ID[Game], players: ProtoPlayers)
