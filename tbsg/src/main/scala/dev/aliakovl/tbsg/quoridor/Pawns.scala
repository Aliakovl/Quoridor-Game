package dev.aliakovl.tbsg.quoridor

import cats.syntax.all.*
import cats.data.{NonEmptySet, Validated, ValidatedNec}
import dev.aliakovl.tbsg.CyclicOrdered.given
import dev.aliakovl.tbsg.quoridor
import dev.aliakovl.tbsg.quoridor.GameError.*
import dev.aliakovl.tbsg.quoridor.Pawn.*
import dev.aliakovl.tbsg.quoridor.PlayersCount.*

case class Pawns(activePawn: Pawn, waitingPawns: NonEmptySet[Pawn])
    extends Iterable[Pawn]:
  override def iterator: Iterator[Pawn] = new Iterator[Pawn] {
    private var isActive: Boolean = true
    private val setIterator = waitingPawns.toSortedSet.iterator

    override def hasNext: Boolean =
      if isActive then isActive else setIterator.hasNext

    override def next(): Pawn = if isActive then {
      isActive = false
      activePawn
    } else setIterator.next()
  }
end Pawns

object Pawns:
  def initialize(
      playersCount: PlayersCount,
      board: Board
  ): Pawns = {
    inline transparent def initializePawn(
        inline side: Side
    ): Pawn = {
      Pawn(
        side,
        board.initializeCell(side),
        board.wallsPerPawn(playersCount)
      )
    }

    playersCount match
      case TwoPlayers =>
        Pawns(
          activePawn = initializePawn(Side.Bottom),
          waitingPawns = NonEmptySet.one(
            initializePawn(Side.Top)
          )
        )
      case FourPlayers =>
        Pawns(
          activePawn = initializePawn(Side.Bottom),
          waitingPawns = NonEmptySet.of(
            initializePawn(Side.Left),
            initializePawn(Side.Top),
            initializePawn(Side.Right)
          )
        )
  }
end Pawns
