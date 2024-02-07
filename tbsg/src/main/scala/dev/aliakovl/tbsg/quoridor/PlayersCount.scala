package dev.aliakovl.tbsg.quoridor

sealed trait PlayersCount:
  def toInt: Int

object PlayersCount:
  case object TwoPlayers extends PlayersCount {
    override def toInt: Int = 2
  }
  case object FourPlayers extends PlayersCount {
    override def toInt: Int = 4
  }
