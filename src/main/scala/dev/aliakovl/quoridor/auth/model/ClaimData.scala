package dev.aliakovl.quoridor.auth.model

import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.tagging.ID

case class ClaimData(userId: ID[User], username: Username)
