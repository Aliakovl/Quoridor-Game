package model.game

import model.GameMoveException
import model.GameMoveException._
import model.game.geometry.{Board, PawnPosition, WallPosition}


trait MoveValidator { this: Move =>
  def validate(game: Game): Either[GameMoveException, Unit]
}

sealed trait Move {
  def makeAt(game: Game): Either[GameMoveException, GameState]
}

case class PawnMove(pawnPosition: PawnPosition) extends Move with MoveValidator {
  override def makeAt(game: Game): Either[GameMoveException, GameState] = {
    for {
      _ <- validate(game)
      activePlayer = game.activePlayer.copy(pawnPosition = pawnPosition)
      players = game.enemyPlayers + activePlayer
      state = game.state.copy(players = players)
    } yield state
  }

  override def validate(game: Game): Either[GameMoveException, Unit] = {
    for {
      _ <- Either.cond(Board.isPawnOnBoard(pawnPosition), (), PawnOutOfBoardException)
      _ <- Either.cond(game.possibleSteps.contains(pawnPosition), (), PawnIllegalMoveException)
    } yield ()
  }
}

case class PlaceWall(wallPosition: WallPosition) extends Move with MoveValidator {
  override def makeAt(game: Game): Either[GameMoveException, GameState] = {
    for {
      _ <- validate(game)
      walls = game.state.walls + wallPosition
      state = game.state.copy(walls = walls)
    } yield state
  }

  override def validate(game: Game): Either[GameMoveException, Unit] = {
    lazy val anyIntersections = game.state.walls
      .map(Board.doWallsIntersect(wallPosition, _))
      .fold(false)(_ || _)

    lazy val noBlocks = game.state.players.map{ player =>
      Board.existsPath(player.pawnPosition, player.target, game.state.walls)
    }.fold(true)(_ && _)

    for {
      _ <- Either.cond(Board.isWallOnBoard(wallPosition), (), WallOutOfBoardException)
      _ <- Either.cond(!anyIntersections, (), WallImpositionException)
      _ <- Either.cond(noBlocks, (), WallBlocksPawls)
    } yield ()
  }
}
