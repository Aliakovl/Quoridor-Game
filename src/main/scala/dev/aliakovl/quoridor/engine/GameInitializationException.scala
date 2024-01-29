package dev.aliakovl.quoridor.engine

sealed abstract class GameInitializationException(message: String)
    extends Exception(message)

object GameInitializationException:
  case object PlayersNumberLimitException
      extends GameInitializationException(
        "The number of players has reached the limit"
      )

  case object NotEnoughPlayersException
      extends GameInitializationException("Not enough players to start game")
