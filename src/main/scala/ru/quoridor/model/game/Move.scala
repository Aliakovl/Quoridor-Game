package ru.quoridor.model.game

import ru.quoridor.model.GameMoveException._
import ru.quoridor.model.GameMoveException
import ru.quoridor.model.game.geometry.{Board, PawnPosition, WallPosition}

trait MoveValidator { self: Move =>
  protected def validate(state: State): Either[GameMoveException, Unit] =
    self match {
      case PawnMove(pawnPosition)  => pawnMoveValidate(state, pawnPosition)
      case PlaceWall(wallPosition) => placeWallValidate(state, wallPosition)
    }

  private def pawnMoveValidate(
      state: State,
      pawnPosition: PawnPosition
  ): Either[GameMoveException, Unit] = {
    for {
      _ <- Either.cond(
        Board.isPawnOnBoard(pawnPosition),
        (),
        PawnOutOfBoardException
      )
      _ <- Either.cond(
        state.possibleSteps.contains(pawnPosition),
        (),
        PawnIllegalMoveException
      )
    } yield ()
  }

  private def placeWallValidate(
      state: State,
      wallPosition: WallPosition
  ): Either[GameMoveException, Unit] = {
    lazy val noIntersections =
      state.walls.forall(!Board.doWallsIntersect(wallPosition, _))

    lazy val walls = state.walls + wallPosition
    lazy val noBlocks = state.players.toList
      .forall { player =>
        Board.existsPath(player.pawnPosition, player.target, walls)
      }

    for {
      _ <- Either.cond(
        state.players.activePlayer.wallsAmount > 0,
        (),
        NotEnoughWall(state.players.activePlayer.id)
      )
      _ <- Either.cond(
        Board.isWallOnBoard(wallPosition),
        (),
        WallOutOfBoardException
      )
      _ <- Either.cond(noIntersections, (), WallImpositionException)
      _ <- Either.cond(noBlocks, (), WallBlocksPawls)
    } yield ()
  }
}

sealed trait Move extends MoveValidator {
  def makeAt(state: State): Either[GameMoveException, State] =
    validate(state).map(_ => legalMove(state))

  protected def legalMove(state: State): State
}

final case class PawnMove(pawnPosition: PawnPosition) extends Move {
  override protected def legalMove(state: State): State = {
    val activePlayer =
      state.players.activePlayer.copy(pawnPosition = pawnPosition)
    val players = state.players.copy(activePlayer = activePlayer)
    state.copy(players = players.shift)
  }
}

final case class PlaceWall(wallPosition: WallPosition) extends Move {
  override protected def legalMove(state: State): State = {
    val walls = state.walls + wallPosition
    val activePlayer = state.players.activePlayer
      .copy(wallsAmount = state.players.activePlayer.wallsAmount - 1)
    val players = state.players.copy(activePlayer = activePlayer)
    State(players.shift, walls)
  }
}

object Move {
  import cats.syntax.functor._
  import sttp.tapir.Schema
  import sttp.tapir.generic.auto.schemaForCaseClass
  import io.circe.{Decoder, Encoder}
  import io.circe.generic.auto._
  import io.circe.syntax._

  implicit val jsonEncoder: Encoder[Move] = Encoder.instance {
    case pm: PawnMove  => pm.asJson
    case pw: PlaceWall => pw.asJson
  }

  implicit val jsonDecoder: Decoder[Move] = List[Decoder[Move]](
    Decoder[PawnMove].widen,
    Decoder[PlaceWall].widen
  ).reduceLeft(_ or _)

  implicit val schema: Schema[Move] = Schema.derivedSchema
}
