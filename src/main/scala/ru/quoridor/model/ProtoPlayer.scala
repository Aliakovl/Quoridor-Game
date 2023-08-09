package ru.quoridor.model

import ru.quoridor.auth.model.Username
import ru.quoridor.model.game.geometry.Side
import ru.utils.tagging.ID

final case class ProtoPlayer(id: ID[User], username: Username, target: Side)
