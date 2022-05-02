package model.game

import model.geometry.{Board, PawnPosition, WallPosition}

trait GameMove {
  def pawnMove(game: Game, pawnPosition: PawnPosition): Option[GameState]

  def placeWall(game: Game, wallPosition: WallPosition): Option[GameState]
}

object GameMove {
  def pawnMove(game: Game, pawnPosition: PawnPosition): Option[GameState] = {
    if (GameMoveValidator.isValidPawnMove(game, pawnPosition)){
      val activePlayer = game.activePlayer.copy(pawnPosition = pawnPosition)
      val players = game.enemyPlayers + activePlayer
      Some(game.state.copy(players = players))
    } else {
      None
    }
  }

  def placeWall(game: Game, wallPosition: WallPosition): Option[GameState] = {
    if (GameMoveValidator.isValidPlaceWall(game, wallPosition)){
      val walls = game.state.walls + wallPosition
      Some(game.state.copy(walls = walls))
    } else {
      None
    }
  }
}


object GameMoveValidator {
  def isValidPawnMove(game: Game, to: PawnPosition): Boolean = {
    Board.isPawnOnBoard(to) && game.possibleSteps.contains(to)
  }

  def isValidPlaceWall(game: Game, wallPosition: WallPosition): Boolean = {
    lazy val noIntersections = game.state.walls
      .map(Board.doWallsIntersect(wallPosition, _))
      .fold(false)(_ || _)

    lazy val noBlocks = game.state.players.map{
      player => Board.existsPath(player.pawnPosition, player.target, game.state.walls)
    }.fold(true)(_ && _)

    Board.isWallOnBoard(wallPosition) && noIntersections && noBlocks
  }

}