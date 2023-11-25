package ru.quoridor.model

import ru.quoridor.auth.model.Username
import ru.quoridor.engine.geometry.Side
import ru.utils.tagging.ID

case class ProtoPlayer(id: ID[User], username: Username, target: Side)
