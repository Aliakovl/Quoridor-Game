package dev.aliakovl.tbsg.quoridor

import cats.syntax.all._
import cats.data.{Validated, ValidatedNec}
import com.softwaremill.quicklens.*
import dev.aliakovl
import dev.aliakovl.tbsg
import dev.aliakovl.tbsg.quoridor
import dev.aliakovl.tbsg.quoridor.Board.*
import dev.aliakovl.tbsg.quoridor.GameError.*
import dev.aliakovl.tbsg.quoridor.GameEvent.*
import dev.aliakovl.tbsg.quoridor.*
import dev.aliakovl.tbsg.quoridor.Orientation.*

import scala.math.Ordering.Implicits.given
import scala.language.implicitConversions

class Board(size: Int):
  private val minCell: Int = 0
  private val midCell: Int = size / 2
  private val maxCell: Int = size - 1
  private val minGroove: Int = minCell
  private val maxGroove: Int = maxCell - 1
  private val totalWalls: Int = 2 * (size + 1)

  private val allWalls: Set[Groove] = (for {
    row <- minGroove to maxGroove
    column <- minGroove to maxGroove
    orientation <- List(Horizontal, Vertical)
  } yield Groove(row, column, orientation)).toSet

  private def validCoordinates(cell: Cell): Boolean = cell match {
    case Cell(row, column) =>
      row.between(minCell, maxCell) && column.between(minCell, maxCell)
  }

  private def validCoordinates(groove: Groove): Boolean = groove match {
    case Groove(row, column, _) =>
      row.between(minGroove, maxGroove) && column.between(minGroove, maxGroove)
  }

  private def allReachableCells(
      from: Cell,
      walls: Set[Groove]
  ): Seq[Cell] = {
    Direction.values.toIndexedSeq.flatMap(reachableCell(from, walls, _))
  }

  def isWinner(pawn: Pawn): Boolean = {
    pawn.edge match {
      case Side.Top    => pawn.position.row == maxCell
      case Side.Bottom => pawn.position.row == minCell
      case Side.Left   => pawn.position.column == maxCell
      case Side.Right  => pawn.position.column == minCell
    }
  }

  def onEdge(pawn: Pawn): Boolean = {
    pawn.edge match {
      case Side.Top    => pawn.position.row == minCell
      case Side.Bottom => pawn.position.row == maxCell
      case Side.Left   => pawn.position.column == minCell
      case Side.Right  => pawn.position.column == maxCell
    }
  }

  inline def wallsPerPawn(inline playersCount: PlayersCount): Int = {
    totalWalls / playersCount.asInt
  }

  inline def initializeCell(inline side: Side): Cell = inline side match {
    case Side.Top    => Cell(minCell, midCell)
    case Side.Bottom => Cell(maxCell, midCell)
    case Side.Left   => Cell(midCell, minCell)
    case Side.Right  => Cell(midCell, maxCell)
  }

  def reachableCell(
      from: Cell,
      walls: Set[Groove],
      direction: Direction
  ): Option[Cell] = {
    val wayIsBlocked = from.grooves(direction).exists(walls.contains)
    if (!wayIsBlocked) {
      val newPosition = from.step(direction)
      Option.when(validCoordinates(newPosition))(newPosition)
    } else None
  }

  def accessibleGrooves(
      pawns: Pawns,
      walls: Set[Groove]
  ): Set[Groove] = {
    (allWalls -- walls)
      .filter { wall =>
        !walls.exists(wall.intersects)
      }
      .filter { wall =>
        pawns.forall { pawn =>
          doPathExists(pawn, walls + wall)
        }
      }
  }

  private def doPathExists(
      pawn: Pawn,
      walls: Set[Groove]
  ): Boolean = {
    import scala.collection.mutable

    val queue = mutable.Queue.empty[Cell]
    val used = mutable.Set.empty[Cell]
    queue.enqueue(pawn.position)
    used.add(pawn.position)
    while (queue.nonEmpty) {
      val position = queue.dequeue()
      if onEdge(pawn) then return true
      for (adjacentPosition <- allReachableCells(position, walls)) {
        if !used.contains(adjacentPosition) then
          used.add(adjacentPosition)
          queue.enqueue(adjacentPosition)
      }
    }
    false
  }

  def move(
      event: GameEvent,
      state: ActiveGame
  ): ValidatedNec[GameError, GameState] = event match {
    case PawnMove(pawnsEdge, toCell) =>
      (
        Validated.condNec(
          state.pawns.activePawn.edge == pawnsEdge,
          (),
          AnotherPawnMoveError
        ),
        Validated.condNec(
          validCoordinates(toCell),
          (),
          OutOfBoardPawnMoveError
        ),
        Validated.condNec(
          state.accessibleSteps(this).contains(toCell),
          (),
          PawnIllegalMoveError
        )
      ).tupled *> movePawn(toCell, state).toValidatedNec
    case PlaceWall(pawnsEdge, toGroove) =>
      (
        Validated.condNec(
          state.pawns.activePawn.edge == pawnsEdge,
          (),
          AnotherPawnMoveError
        ),
        Validated.condNec(
          state.pawns.activePawn.walls > 0,
          (),
          PlayerHasNotEnoughWallsToPlace
        ),
        Validated.condNec(
          validCoordinates(toGroove),
          (),
          OutOfBoardPlaceWallError
        ),
        Validated.condNec(
          !state.walls.exists(_.intersects(toGroove)),
          (),
          WallsIntersects
        ),
        Validated.condNec(
          state.pawns.forall(pawn =>
            doPathExists(pawn, state.walls + toGroove)
          ),
          (),
          WallBlocksPathForPawn
        ),
      ).tupled *> placeWall(toGroove, state).toValidatedNec
  }

  private def movePawn(
      toCell: Cell,
      state: ActiveGame
  ): Validated[GameError, GameState] = {
    state
      .modify(_.pawns.activePawn.position)
      .setTo(toCell)
      .nextTurn(this)
  }

  private def placeWall(
      toGroove: Groove,
      state: ActiveGame
  ): Validated[GameError, GameState] = {
    state
      .modify(_.walls)(_ + toGroove)
      .modify(_.pawns.activePawn.walls)(_ - 1)
      .nextTurn(this)
  }
end Board

object Board:
  extension [N: Ordering](num: N)
    def between(from: N, to: N): Boolean = from <= num && num <= to
end Board
