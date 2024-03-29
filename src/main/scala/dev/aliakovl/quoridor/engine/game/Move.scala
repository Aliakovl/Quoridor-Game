package dev.aliakovl.quoridor.engine.game

import dev.aliakovl.quoridor.engine.GameMoveException
import dev.aliakovl.quoridor.engine.GameMoveException.*
import dev.aliakovl.quoridor.engine.game.geometry.*
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.tagging.ID

trait MoveValidator { self: Move =>
  protected def validate(state: State): Either[GameMoveException, Unit] =
    self match {
      case Move.PawnMove(pawnPosition) => pawnMoveValidate(state, pawnPosition)
      case Move.PlaceWall(wallPosition) =>
        placeWallValidate(state, wallPosition)
    }

  private def pawnMoveValidate(
      state: State,
      pawnPosition: PawnPosition
  ): Either[GameMoveException, Unit] = {
    for {
      _ <- Either.cond(
        Board.isPawnOnBoard(pawnPosition),
        (),
        PawnOutOfBoardException
      )
      _ <- Either.cond(
        state.possibleSteps.contains(pawnPosition),
        (),
        PawnIllegalMoveException
      )
    } yield ()
  }

  private def placeWallValidate(
      state: State,
      wallPosition: WallPosition
  ): Either[GameMoveException, Unit] = {
    lazy val noIntersections =
      state.walls.forall(!Board.doWallsIntersect(wallPosition, _))

    lazy val walls = state.walls + wallPosition
    lazy val noBlocks = state.players.toList
      .forall { player =>
        Board.existsPath(player.pawnPosition, player.target, walls)
      }

    for {
      _ <- Either.cond(
        state.players.activePlayer.wallsAmount > 0,
        (),
        NotEnoughWall(state.players.activePlayer.id)
      )
      _ <- Either.cond(
        Board.isWallOnBoard(wallPosition),
        (),
        WallOutOfBoardException
      )
      _ <- Either.cond(noIntersections, (), WallImpositionException)
      _ <- Either.cond(noBlocks, (), WallBlocksPawls)
    } yield ()
  }
}

enum Move extends MoveValidator { self =>
  case PawnMove(pawnPosition: PawnPosition) extends Move
  case PlaceWall(wallPosition: WallPosition) extends Move

  def makeAt(state: State): Either[GameMoveException, State] =
    validate(state).map(_ => legalMove(state))

  def getWinner(state: State): Option[ID[User]] = {
    Some(self).collect { case Move.PawnMove(pawnPosition) =>
      Some(state.players.activePlayer).collect {
        case Player(id, _, _, target)
            if Board.isPawnOnEdge(pawnPosition, target) =>
          id
      }
    }.flatten
  }

  private def legalMove(state: State): State = self match
    case PawnMove(pawnPosition: PawnPosition) =>
      val activePlayer =
        state.players.activePlayer.copy(pawnPosition = pawnPosition)
      val players = state.players.copy(activePlayer = activePlayer)
      state.copy(players = players.shift)
    case PlaceWall(wallPosition: WallPosition) =>
      val walls = state.walls + wallPosition
      val activePlayer = state.players.activePlayer
        .copy(wallsAmount = state.players.activePlayer.wallsAmount - 1)
      val players = state.players.copy(activePlayer = activePlayer)
      State(players.shift, walls)
}
