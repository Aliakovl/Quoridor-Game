package ru.quoridor.model

import java.util.UUID

sealed abstract class GameException(message: String) extends Exception(message)

object GameException {
  case class UserNotFoundException(userId: UUID)
      extends GameException(s"User with id=$userId not found")

  case class LoginNotFoundException(login: String)
      extends GameException(s"User with login=$login not found")

  case class GameNotFoundException(gameId: UUID)
      extends GameException(s"Game with id=$gameId not found")

  case class WrongPlayersTurnException(gameId: UUID)
      extends GameException(
        s"It is another player`s turn in the game with id=$gameId"
      )

  case class GameInterloperException(userId: UUID, gameId: UUID)
      extends GameException(
        s"User with id=$userId does not belong to the game with id=$gameId"
      )

  case object NotEnoughPlayersException
      extends GameException("Not enough players to start game")

  case object PlayersNumberLimitException
      extends GameException("The number of players has reached the limit")

  case class LoginOccupiedException(login: String)
      extends GameException(s"User with login $login already exists")

  case class SamePlayerException(userId: UUID, gameId: UUID)
      extends GameException(
        s"User with id=$userId already belong to the game with id=$gameId"
      )

  case class NotGameCreatorException(userId: UUID, gameId: UUID)
      extends GameException(
        s"User with id=$userId did not create the game with id=$gameId"
      )

  case class GameAlreadyStartedException(gameId: UUID)
      extends GameException(s"Game with id=$gameId has already started")

  case class GameHasFinishedException(gameId: UUID)
      extends GameException(s"Game with id=$gameId has already finished")
}

sealed abstract class GameMoveException(message: String)
    extends Exception(message)

object GameMoveException {
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

  case class NotEnoughWall(userId: UUID)
      extends GameMoveException(
        s"Player with id=$userId does not have walls to place"
      )
}
