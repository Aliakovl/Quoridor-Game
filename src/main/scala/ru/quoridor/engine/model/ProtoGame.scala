package ru.quoridor.engine.model

import ru.quoridor.engine.model.game.Game
import ru.utils.tagging.ID

case class ProtoGame(id: ID[Game], players: ProtoPlayers)
