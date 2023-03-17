package ru.quoridor.dao.dto

import ru.quoridor.auth.model.{UserSecret, Username}

case class Userdata(userId: UserId, username: Username, userSecret: UserSecret)
