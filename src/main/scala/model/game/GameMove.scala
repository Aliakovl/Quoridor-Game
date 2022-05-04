package model.game

import model.game.geometry.{Board, PawnPosition, WallPosition}


trait MoveValidator { this: Move =>
  def validate(game: Game): Boolean
}

sealed trait Move {
  def makeOn(game: Game): Option[GameState]
}

case class PawnMove(pawnPosition: PawnPosition) extends Move with MoveValidator {
  override def makeOn(game: Game): Option[GameState] = {
    if (validate(game)){
      val activePlayer = game.activePlayer.copy(pawnPosition = pawnPosition)
      val players = game.enemyPlayers + activePlayer
      Some(game.state.copy(players = players))
    } else {
      None
    }
  }

  override def validate(game: Game): Boolean = {
    Board.isPawnOnBoard(pawnPosition) && game.possibleSteps.contains(pawnPosition)
  }
}

case class PlaceWall(wallPosition: WallPosition) extends Move with MoveValidator {
  override def makeOn(game: Game): Option[GameState] = {
    if (validate(game)){
      val walls = game.state.walls + wallPosition
      Some(game.state.copy(walls = walls))
    } else {
      None
    }
  }

  override def validate(game: Game): Boolean = {
    lazy val anyIntersections = game.state.walls
      .map(Board.doWallsIntersect(wallPosition, _))
      .fold(false)(_ || _)

    lazy val noBlocks = game.state.players.map{
      player => {
        val g = Board.existsPath(player.pawnPosition, player.target, game.state.walls)
        println((g, player.pawnPosition, player.target, game.state.walls))
        g
      }
    }.fold(true)(_ && _)

    val a = Board.isWallOnBoard(wallPosition)
    val b = !anyIntersections
    val c = noBlocks

    println((a, b, c))

    a && b && c
  }
}
