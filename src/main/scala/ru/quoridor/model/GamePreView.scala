package ru.quoridor.model

import ru.quoridor.model.game.Game
import ru.utils.Tagged.ID

case class GamePreView(id: ID[Game], players: List[User], winner: Option[User])
