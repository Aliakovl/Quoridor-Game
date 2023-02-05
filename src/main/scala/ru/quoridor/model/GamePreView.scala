package ru.quoridor.model

import ru.quoridor.model.game.Game
import ru.utils.Typed.ID

case class GamePreView(id: ID[Game], players: List[User], winner: Option[User])
