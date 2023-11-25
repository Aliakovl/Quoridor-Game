package ru.quoridor.engine.model.game

import ru.quoridor.engine.model.User
import ru.utils.tagging.ID

case class Game(id: ID[Game], step: Int, state: State, winner: Option[User])
