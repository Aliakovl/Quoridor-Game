package ru.quoridor.engine.model.game

import cats.data.NonEmptyList
import ru.utils.Shifting

case class Players(activePlayer: Player, enemies: NonEmptyList[Player]) {
  lazy val toList: List[Player] = activePlayer +: enemies.toList

  def shift: Players = {
    Shifting[Player](activePlayer, enemies).shift match {
      case Shifting(p, pp) => Players(p, pp)
    }
  }
}
