package ru.quoridor.model

import ru.quoridor.auth.model.Username
import ru.quoridor.model.game.Game
import ru.utils.tagging.Id

sealed abstract class GameException(message: String) extends Exception(message)

object GameException {
  final case class UserNotFoundException(userId: Id[User])
      extends GameException(s"User with id=$userId not found")

  final case class UsernameNotFoundException(username: Username)
      extends GameException(s"User with username=$username not found")

  final case class GameNotFoundException(gameId: Id[Game])
      extends GameException(s"Game with id=$gameId not found")

  final case class WrongPlayersTurnException(gameId: Id[Game])
      extends GameException(
        s"It is another player`s turn in the game with id=$gameId"
      )

  final case class GameInterloperException(userId: Id[User], gameId: Id[Game])
      extends GameException(
        s"User with id=$userId does not belong to the game with id=$gameId"
      )

  case object NotEnoughPlayersException
      extends GameException("Not enough players to start game")

  case object PlayersNumberLimitException
      extends GameException("The number of players has reached the limit")

  final case class UsernameOccupiedException(username: Username)
      extends GameException(s"User with username $username already exists")

  final case class SamePlayerException(userId: Id[User], gameId: Id[Game])
      extends GameException(
        s"User with id=$userId already belong to the game with id=$gameId"
      )

  final case class NotGameCreatorException(userId: Id[User], gameId: Id[Game])
      extends GameException(
        s"User with id=$userId did not create the game with id=$gameId"
      )

  final case class GameAlreadyStartedException(gameId: Id[Game])
      extends GameException(s"Game with id=$gameId has already started")

  final case class GameHasFinishedException(gameId: Id[Game])
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

  final case class NotEnoughWall(userId: Id[User])
      extends GameMoveException(
        s"Player with id=$userId does not have walls to place"
      )
}
