package model.game

import model.User
import utils.Typed.ID


case class Game(id: ID[Game], state: State, winner: Option[User])