package ru.quoridor.auth.model

import ru.quoridor.engine.model.User
import ru.utils.tagging.ID

case class ClaimData(userId: ID[User], username: Username)
