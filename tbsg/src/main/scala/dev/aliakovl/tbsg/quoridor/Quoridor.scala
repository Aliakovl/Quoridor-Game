package dev.aliakovl.tbsg.quoridor

final class Quoridor(size: Int) {
  private val board: Board = new Board(size)
  val rules = new QuoridorRules(board)
  val actions = new QuoridorActions(board)
}
