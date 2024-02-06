package dev.aliakovl.quoridor

import dev.aliakovl.quoridor.auth.model.Username
import dev.aliakovl.quoridor.model.{Game, User}
import dev.aliakovl.utils.tagging.ID

sealed abstract class GameException(message: String) extends Exception(message)

object GameException:
  case class UserNotFoundException(userId: ID[User]) // dao
      extends GameException(s"User with id=$userId not found")

  case class UsernameNotFoundException(username: Username) // dao
      extends GameException(s"User with username=$username not found")

  case object UsersNotFoundException // dao
      extends GameException(s"Users not found")
  type UsersNotFoundException = UsersNotFoundException.type

  case class GameNotFoundException(gameId: ID[Game]) // dao
      extends GameException(s"Game with id=$gameId not found")

  case class WrongPlayersTurnException(gameId: ID[Game])
      extends GameException(
        s"It is another player`s turn in the game with id=$gameId"
      )

  case class GameInterloperException(userId: ID[User], gameId: ID[Game])
      extends GameException(
        s"User with id=$userId does not belong to the game with id=$gameId"
      )

  case class UsernameOccupiedException(username: Username) // dao
      extends GameException(s"User with username $username already exists")

  case class SamePlayerException(userId: ID[User], gameId: ID[Game]) // dao
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
