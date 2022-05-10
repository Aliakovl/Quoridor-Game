package model.game

import model.game.geometry.Direction._
import model.game.geometry._
import utils.Typed.ID


case class Game(id: ID[Game],
                state: GameState) {
  lazy val possibleSteps: List[PawnPosition] = {
    List(
      possibleStep(_, ToNorth),
      possibleStep(_, ToSouth),
      possibleStep(_, ToWest),
      possibleStep(_, ToEast)
    ).flatMap(f => f(state.players.activePlayer))
  }

  private def possibleStep(player: Player, direction: Direction): List[PawnPosition] = {
    val pawnPosition = player.pawnPosition
    val walls = state.walls
    Board.adjacentPosition(pawnPosition, walls, direction) match {
      case None => List.empty
      case Some(position) =>
        state.players.enemies.find(_.pawnPosition == position) match {
          case None => List(position)
          case Some(enemy) =>
            Board.adjacentPosition(enemy.pawnPosition, walls, direction) match {
              case Some(_) => possibleStep(enemy, direction)
              case None =>
                val (left, right) = direction.crossed
                possibleStep(enemy, left) ++ possibleStep(enemy, right)
            }
        }
    }
  }
}
