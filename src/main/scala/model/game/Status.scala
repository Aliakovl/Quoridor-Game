package model.game

sealed trait Status

object Status {
  case object InProgress extends Status
  case class HasWinner(player: Player) extends Status
}
