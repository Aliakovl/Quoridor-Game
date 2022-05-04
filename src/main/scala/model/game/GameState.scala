package model.game

import model.game.geometry.WallPosition

case class GameState(players: Set[Player], walls: Set[WallPosition])
