package ru.quoridor.model

import ru.quoridor.engine.Game
import ru.utils.tagging.ID

case class ProtoGame(id: ID[Game], players: ProtoPlayers)
