package dev.aliakovl.quoridor.dao.dto

import dev.aliakovl.quoridor.auth.model.{UserSecret, Username}

case class Userdata(userId: UserId, username: Username, userSecret: UserSecret)
