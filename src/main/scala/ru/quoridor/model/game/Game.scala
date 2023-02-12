package ru.quoridor.model.game

import ru.quoridor.model.User
import ru.utils.tagging.ID

case class Game(id: ID[Game], state: State, winner: Option[User])
