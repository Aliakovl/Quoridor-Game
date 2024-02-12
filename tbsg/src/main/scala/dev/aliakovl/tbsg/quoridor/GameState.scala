package dev.aliakovl.tbsg.quoridor

import cats.data.{NonEmptySet, Validated, ValidatedNec}
import cats.syntax.all.*
import dev.aliakovl.tbsg.CyclicOrdered.given
import dev.aliakovl.tbsg.quoridor.GameError.WrongWaitingPawnsError

sealed trait GameState

object GameState:
  case class ActiveGame(
      pawns: Pawns,
      walls: Set[Groove],
      winnersTable: Seq[Pawn]
  ) extends GameState {

    def nextTurn(board: Board): Validated[GameError, GameState] = {
      Validated
        .fromOption(
          pawns.waitingPawns.clockwise(pawns.activePawn),
          WrongWaitingPawnsError
        )
        .map { newActivePawn =>
          if board.isWinner(pawns.activePawn) then {
            NonEmptySet.fromSet(pawns.waitingPawns - newActivePawn) match
              case Some(newWaitingPawns) =>
                ActiveGame(
                  Pawns(newActivePawn, newWaitingPawns),
                  walls,
                  winnersTable :+ pawns.activePawn
                )
              case None => EndedGame(walls, winnersTable :+ pawns.activePawn)
          } else {
            ActiveGame(
              Pawns(
                newActivePawn,
                NonEmptySet(
                  pawns.activePawn,
                  pawns.waitingPawns - newActivePawn
                )
              ),
              walls,
              winnersTable
            )
          }
        }
    }

    def accessibleSteps(board: Board): Seq[Cell] = {
      Direction.values.toIndexedSeq.flatMap {
        accessibleStep(_, board)
      }
    }

    private def accessibleStep(direction: Direction, board: Board): Seq[Cell] =
      _accessibleStep(pawns.activePawn, direction, board)

    private def _accessibleStep(
        pawn: Pawn,
        direction: Direction,
        board: Board
    ): Seq[Cell] = {
      board.reachableCell(pawn.position, walls, direction) match
        case None => Seq.empty
        case Some(position) =>
          pawns.waitingPawns.find(_.position == position) match
            case None => Seq(position)
            case Some(waitingPawn) =>
              board.reachableCell(waitingPawn.position, walls, direction) match
                case Some(_) => _accessibleStep(waitingPawn, direction, board)
                case None =>
                  val (left, right) = direction.crossed
                  _accessibleStep(waitingPawn, left, board) ++
                    _accessibleStep(waitingPawn, right, board)
    }
  }

  case class EndedGame(walls: Set[Groove], winnersTable: Seq[Pawn])
      extends GameState

end GameState
