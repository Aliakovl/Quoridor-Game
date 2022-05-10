package model.game

import cats.data.NonEmptyList
import model.game.geometry.WallPosition
import utils.Shifting

case class GameState(players: Players, walls: Set[WallPosition])

case class Players(activePlayer: Player, enemies: NonEmptyList[Player]) {
  lazy val toList: List[Player] = activePlayer +: enemies.toList

  def shift: Players = {
    new Shifting[Player](activePlayer, enemies).shift match {
      case Shifting(p, pp) => Players(p, pp)
    }
  }
}
