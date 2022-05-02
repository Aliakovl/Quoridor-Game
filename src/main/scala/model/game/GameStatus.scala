package model.game

sealed trait GameStatus

object GameStatus {
  case object InProgress extends GameStatus
  case class HasWinner(player: Player) extends GameStatus
}
