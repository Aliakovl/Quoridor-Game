package ru.quoridor.auth.model

import ru.quoridor.model.User
import ru.utils.tagging.ID

final case class ClaimData(userId: ID[User], username: Username)
