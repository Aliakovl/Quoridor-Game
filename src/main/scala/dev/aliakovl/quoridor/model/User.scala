package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.utils.tagging.ID

case class User(id: ID[User], username: Username)
