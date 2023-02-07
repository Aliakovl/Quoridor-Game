package ru.quoridor.model

import ru.utils.Tagged.ID

case class User(id: ID[User], login: String)
