package model.game

import utils.Typed.ID


case class Game(id: ID[Game], state: State)