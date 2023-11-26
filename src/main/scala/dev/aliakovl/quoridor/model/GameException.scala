package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.engine.Game
import dev.aliakovl.utils.tagging.ID

sealed abstract class GameException(message: String) extends Exception(message)

object GameException:
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
