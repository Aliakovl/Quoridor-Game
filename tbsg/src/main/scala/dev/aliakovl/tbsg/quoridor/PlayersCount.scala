package dev.aliakovl.tbsg.quoridor

sealed trait PlayersCount:
  def asInt: Int

object PlayersCount:
  case object TwoPlayers extends PlayersCount:
    override val asInt: Int = 2

  case object FourPlayers extends PlayersCount:
    override val asInt: Int = 4
end PlayersCount
