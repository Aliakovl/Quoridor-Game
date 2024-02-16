package dev.aliakovl.tbsg.quoridor

import cats.data.NonEmptySet
import dev.aliakovl.tbsg.quoridor
import dev.aliakovl.tbsg.quoridor.Pawn.*
import dev.aliakovl.tbsg.quoridor.PlayersCount.*

case class Pawns(activePawn: Pawn, waitingPawns: NonEmptySet[Pawn])

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

  given Conversion[Pawns, Iterator[Pawn]] with
    override def apply(pawns: Pawns): Iterator[Pawn] = new Iterator[Pawn] {
      private var isActive: Boolean = true
      private val setIterator = pawns.waitingPawns.toSortedSet.iterator

      override def hasNext: Boolean =
        if isActive then isActive else setIterator.hasNext

      override def next(): Pawn = if isActive then {
        isActive = false
        pawns.activePawn
      } else setIterator.next()
    }
end Pawns
