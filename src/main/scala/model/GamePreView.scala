package model

import model.game.Game
import utils.Typed.ID

case class GamePreView(id: ID[Game], players: List[User], winner: Option[User])
