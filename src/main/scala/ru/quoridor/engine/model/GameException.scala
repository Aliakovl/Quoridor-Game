package ru.quoridor.engine.model

import ru.quoridor.auth.model.Username
import ru.quoridor.engine.model.game.Game
import ru.utils.tagging.ID

sealed abstract class GameException(message: String) extends Exception(message)

object GameException {
  case class UserNotFoundException(userId: ID[User])
      extends GameException(s"User with id=$userId not found")

  case class UsernameNotFoundException(username: Username)
      extends GameException(s"User with username=$username not found")

  case class GameNotFoundException(gameId: ID[Game])
      extends GameException(s"Game with id=$gameId not found")

  case class WrongPlayersTurnException(gameId: ID[Game])
      extends GameException(
        s"It is another player`s turn in the game with id=$gameId"
      )

  case class GameInterloperException(userId: ID[User], gameId: ID[Game])
      extends GameException(
        s"User with id=$userId does not belong to the game with id=$gameId"
      )

  case object NotEnoughPlayersException
      extends GameException("Not enough players to start game")

  case object PlayersNumberLimitException
      extends GameException("The number of players has reached the limit")

  case class UsernameOccupiedException(username: Username)
      extends GameException(s"User with username $username already exists")

  case class SamePlayerException(userId: ID[User], gameId: ID[Game])
      extends GameException(
        s"User with id=$userId already belong to the game with id=$gameId"
      )

  case class NotGameCreatorException(userId: ID[User], gameId: ID[Game])
      extends GameException(
        s"User with id=$userId did not create the game with id=$gameId"
      )

  case class GameAlreadyStartedException(gameId: ID[Game])
      extends GameException(s"Game with id=$gameId has already started")

  case class GameHasFinishedException(gameId: ID[Game])
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

  case class NotEnoughWall(userId: ID[User])
      extends GameMoveException(
        s"Player with id=$userId does not have walls to place"
      )
}
