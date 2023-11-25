package ru.quoridor.engine.model

import ru.quoridor.engine.model.game.Game
import ru.utils.tagging.ID

case class GamePreView(id: ID[Game], players: List[User], winner: Option[User])
