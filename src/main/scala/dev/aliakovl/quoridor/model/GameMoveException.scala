package dev.aliakovl.quoridor.model

import dev.aliakovl.utils.tagging.ID

sealed abstract class GameMoveException(message: String)
    extends Exception(message)

object GameMoveException:
  case object PawnOutOfBoardException
      extends GameMoveException("Illegal attempt to move a pawn over the board")

  case object PawnIllegalMoveException
      extends GameMoveException("Attempt to move a pawn to illegal position")

  case object WallOutOfBoardException
      extends GameMoveException(
        "Illegal attempt to place a wall over the board"
      )

  case object WallImpositionException
      extends GameMoveException(
        "Illegal attempt to place a wall on another wall"
      )

  case object WallBlocksPawls
      extends GameMoveException(
        "Illegal attempt to completely block off way to target for some pawls"
      )

  case class NotEnoughWall(userId: ID[User])
      extends GameMoveException(
        s"Player with id=$userId does not have walls to place"
      )
