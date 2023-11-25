package ru.quoridor.model

import ru.quoridor.engine.Game
import ru.utils.tagging.ID

case class GamePreView(id: ID[Game], players: List[User], winner: Option[User])
