package dev.aliakovl.quoridor.engine

import GameMoveException.*
import dev.aliakovl.quoridor.engine.geometry.{PawnPosition, WallPosition}

enum Move extends MoveValidator { self =>
  case PawnMove(pawnPosition: PawnPosition) extends Move
  case PlaceWall(wallPosition: WallPosition) extends Move

  def makeAt(state: State): Either[GameMoveException, State] =
    validate(state).map(_ => legalMove(state))

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
