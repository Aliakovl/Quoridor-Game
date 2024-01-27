package dev.aliakovl.quoridor.engine.game

import dev.aliakovl.quoridor.engine.game.geometry.Direction.*
import dev.aliakovl.quoridor.engine.game.geometry.{
  Board,
  Direction,
  PawnPosition,
  WallPosition
}

case class State(players: Players, walls: Set[WallPosition]) {
  lazy val possibleSteps: List[PawnPosition] = {
    List(
      possibleStep(_, ToNorth),
      possibleStep(_, ToSouth),
      possibleStep(_, ToWest),
      possibleStep(_, ToEast)
    ).flatMap(f => f(players.activePlayer))
  }

  private def possibleStep(
      player: Player,
      direction: Direction
  ): List[PawnPosition] = {
    val pawnPosition = player.pawnPosition
    Board.adjacentPosition(pawnPosition, walls, direction) match {
      case None => List.empty
      case Some(position) =>
        players.enemies.find(_.pawnPosition == position) match {
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
