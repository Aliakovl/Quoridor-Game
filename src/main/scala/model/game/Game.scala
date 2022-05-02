package model.game

import model.geometry._

import java.util.UUID

case class Game(id: UUID,
                activePlayer: Player,
                state: GameState) {
  lazy val enemyPlayers: Set[Player] = state.players - activePlayer

  lazy val possibleSteps: List[PawnPosition] = {
    List(
      possibleStep(activePlayer, Up),
      possibleStep(activePlayer, Down),
      possibleStep(activePlayer, ToLeft),
      possibleStep(activePlayer, ToRight)
    ).flatten
  }

  private def possibleStep(player: Player, direction: Direction): List[PawnPosition] = {
    val pawnPosition = player.pawnPosition
    val walls = state.walls
    Board.adjacentPosition(pawnPosition, walls, direction) match {
      case None => List.empty
      case Some(position) =>
        enemyPlayers.find(_.pawnPosition == position) match {
          case None => List(position)
          case Some(enemy) =>
            Board.adjacentPosition(enemy.pawnPosition, walls, direction) match {
              case Some(_) => possibleStep(enemy, direction)
              case None =>
                val (left, right) = direction.crossDirections
                possibleStep(enemy, left) ++ possibleStep(enemy, right)
            }
        }
    }
  }

}


case class GameState(players: Set[Player], walls: Set[WallPosition])