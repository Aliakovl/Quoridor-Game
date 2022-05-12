package model

import model.game.{Game, Status}
import utils.Typed.ID

case class GameView(id: ID[Game], players: List[User], status: Status)
