package dev.aliakovl.quoridor.engine.game

import dev.aliakovl.quoridor.engine.game.geometry.Direction.*
import dev.aliakovl.quoridor.engine.game.geometry.*

case class State(players: Players, walls: Set[WallPosition] = Set.empty):
  lazy val possibleSteps: List[PawnPosition] = {
    List(
      possibleStep(_, ToNorth),
      possibleStep(_, ToSouth),
      possibleStep(_, ToWest),
      possibleStep(_, ToEast)
    ).flatMap(f => f(players.activePlayer))
  }

  lazy val availableWalls: Set[WallPosition] = {
    if players.activePlayer.wallsAmount > 0 then
      Board.availableWalls(
        walls,
        players.toList.map(player => (player.pawnPosition, player.target))
      )
    else Set.empty[WallPosition]
  }

  private def possibleStep(
      player: Player,
      direction: Direction
  ): List[PawnPosition] =
    Board.adjacentPosition(player.pawnPosition, walls, direction) match {
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
