package model.game

import model.geometry._
//import cats.implicits.catsSyntaxSemigroup


sealed trait MoveResult
object MoveResult {
  object Success extends MoveResult
  object Failure extends MoveResult
  object End     extends MoveResult
}


trait Quoridor {
  val turn: Player
  val playersState: Map[Player, (GridPosition, Int)]
  val wallsState: Set[WallPosition]
  val wallVacancies: Set[WallPosition]
  def allPossibleSteps(player: Player): List[GridPosition]
  def movePawn(player: Player, to: GridPosition): MoveResult
  def placeWall(player: Player, at: WallPosition): MoveResult
}


object Quoridor {
  private val width: Int = 9
  private val halfWidth: Int = width / 2
  private val positionsInit: List[GridPosition] =
    List(
      (0, halfWidth),
      (width, halfWidth),
      (halfWidth, 0),
      (halfWidth, width)
    ).map(GridPosition.tupled)

  private val borders: Seq[WallPosition] = for {
    index <- 0 until width
    if index % 2 == 0
    top = WallPosition(Horizontal, 0, index)
    bottom = WallPosition(Horizontal, width, index)
    left = WallPosition(Vertical, 0, index)
    right = WallPosition(Vertical, width, index)
    elem <- Seq(top, bottom, left, right)
  } yield elem


  def apply(players: Iterable[Player], first: Player): Quoridor = new Quoridor {

    override val playersState: Map[Player, (GridPosition, Int)] = {
      (players zip positionsInit).map { case (player, position) =>
        player -> (position, 21 / players.size)
      }.toMap
    }

    override val wallsState: Set[WallPosition] = borders.toSet

    override def allPossibleSteps(player: Player): List[GridPosition] = {
      possibleSteps(player, Up) ++
        possibleSteps(player, Down) ++
        possibleSteps(player, ToLeft) ++
        possibleSteps(player, ToRight)
    }

    private def adjacentPosition(player: Player, direction: Direction): Option[GridPosition] = for {
      (playerPosition, _) <- playersState.get(player)
      wp1 = direction.wallPosition(playerPosition)
      wp2 = wp1.copy(column = wp1.column - 1)
      bool = (wallsState contains wp1) || (wallsState contains wp2)
      res <- if (bool) None else Some(direction.oneStep(playerPosition))
    } yield res

    private def enemyAtCell(player: Player, cell: GridPosition): Option[Player] = {
      val enemies = playersState - player
      for {
        (enemy, _) <- enemies.find{case (_, (position, _)) => position == cell}
      } yield enemy
    }

    private def possibleSteps(player: Player, direction: Direction): List[GridPosition] = {
      val loop: Either[List[GridPosition], Unit] = for {
        adjacent <- adjacentPosition(player, direction).toRight(List.empty[GridPosition])
        enemy <- enemyAtCell(player, adjacent).toRight(List(adjacent))
        _ <- adjacentPosition(enemy, direction).toRight{
          val (left, right) = direction.crossDirections
          possibleSteps(enemy, left) ++ possibleSteps(enemy, right)
        }
        _ <- Left(possibleSteps(enemy, direction))
      } yield ()

      loop match {
        case Left(value) => value
        case Right(_) => List()
      }
    }


//    def possibleSteps(player: Player, direction: Direction): List[GridPosition] = {
//      adjacentPosition(player, direction) match {
//        case None => List.empty[GridPosition]
//        case Some(adjacent) => {
//          enemyAtCell(player, adjacent) match {
//            case None => List(adjacent)
//            case Some(enemy) => {
//              adjacentPosition(enemy, direction) match {
//                case None => {
//                  val (left, right) = direction.crossSteps
//                  possibleSteps(enemy, left) ++ possibleSteps(enemy, right)
//                }
//                case Some(_) => {
//                  possibleSteps(enemy, direction)
//                }
//              }
//            }
//          }
//        }
//      }
//    }


    override val turn: Player = first

    override val wallVacancies: Set[WallPosition] = _

    override def movePawn(player: Player, to: GridPosition): Quoridor = ???

    override def placeWall(player: Player, at: WallPosition): Quoridor = ???
  }

}
