package model.game

import model.GameMoveException
import model.GameMoveException._
import model.game.geometry.{Board, PawnPosition, WallPosition}


trait MoveValidator { self: Move =>
  def validate(game: Game): Either[GameMoveException, Unit] = self match {
    case PawnMove(pawnPosition) => pawnMoveValidate(game, pawnPosition)
    case PlaceWall(wallPosition) => placeWallValidate(game, wallPosition)
  }

  private def pawnMoveValidate(game: Game, pawnPosition: PawnPosition): Either[GameMoveException, Unit] = {
    for {
      _ <- Either.cond(Board.isPawnOnBoard(pawnPosition), (), PawnOutOfBoardException)
      _ <- Either.cond(game.possibleSteps.contains(pawnPosition), (), PawnIllegalMoveException)
    } yield ()
  }

  private def placeWallValidate(game: Game, wallPosition: WallPosition): Either[GameMoveException, Unit] = {
    lazy val anyIntersections = game.state.walls
      .map(Board.doWallsIntersect(wallPosition, _))
      .fold(false)(_ || _)

    lazy val noBlocks = game.state.players.toList.map{ player =>
      Board.existsPath(player.pawnPosition, player.target, game.state.walls)
    }.fold(true)(_ && _)

    for {
      _ <- Either.cond(game.state.players.activePlayer.wallsAmount > 0, (), NotEnoughWall(game.state.players.activePlayer.id))
      _ <- Either.cond(Board.isWallOnBoard(wallPosition), (), WallOutOfBoardException)
      _ <- Either.cond(!anyIntersections, (), WallImpositionException)
      _ <- Either.cond(noBlocks, (), WallBlocksPawls)
    } yield ()
  }
}

sealed trait Move extends MoveValidator {
  def makeAt(game: Game): Either[GameMoveException, GameState] =
    validate(game).map(_ => legalMove(game))

  protected def legalMove(game: Game): GameState
}

case class PawnMove(pawnPosition: PawnPosition) extends Move {
  override protected def legalMove(game: Game): GameState = {
    val activePlayer = game.state.players.activePlayer.copy(pawnPosition = pawnPosition)
    val players = game.state.players.copy(activePlayer = activePlayer)
    game.state.copy(players = players.shift)
  }
}

case class PlaceWall(wallPosition: WallPosition) extends Move {
  override protected def legalMove(game: Game): GameState = {
    val walls = game.state.walls + wallPosition
    val activePlayer = game.state.players.activePlayer.copy(wallsAmount = game.state.players.activePlayer.wallsAmount - 1)
    val players = game.state.players.copy(activePlayer = activePlayer)
    game.state.copy(players.shift, walls)
  }
}
