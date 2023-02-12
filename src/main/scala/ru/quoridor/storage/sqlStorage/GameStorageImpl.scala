package ru.quoridor.storage.sqlStorage

import cats.data.NonEmptyList
import cats.free.Free
import doobie.ConnectionIO
import doobie.free.connection.{ConnectionOp, rollback}
import doobie.implicits._
import doobie.postgres.sqlstate.class23.FOREIGN_KEY_VIOLATION
import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model.GameException.GameNotFoundException
import ru.quoridor.model.game.geometry.{PawnPosition, WallPosition}
import ru.quoridor.model.game._
import ru.quoridor.storage.{DataBase, GameStorage}
import ru.utils.tagging.ID
import zio.Task
import zio.interop.catz._

class GameStorageImpl(dataBase: DataBase) extends GameStorage {
  override def find(gameId: ID[Game]): Task[Game] = {
    val query = for {
      step <- findLastStep(gameId)
      enemies <- findEnemiesByGameId(gameId, step)
      activePlayer <- findActivePlayerByGameId(gameId, step)
      walls <- findWallsByGameId(gameId, step)
      winner <- findWinnerByGameId(gameId)
    } yield Game(
      gameId,
      step,
      State(Players(activePlayer, enemies), walls),
      winner
    )

    dataBase.transact(query.transact[Task])
  }

  override def find(gameId: ID[Game], step: Int): Task[Game] = {
    val query = for {
      enemies <- findEnemiesByGameId(gameId, step)
      activePlayer <- findActivePlayerByGameId(gameId, step)
      walls <- findWallsByGameId(gameId, step)
      winner <- findWinnerByGameId(gameId)
    } yield Game(
      gameId,
      step,
      State(Players(activePlayer, enemies), walls),
      winner
    )

    dataBase.transact(query.transact[Task])
  }

  override def lastStep(gameId: ID[Game]): Task[Int] = {
    dataBase.transact {
      findLastStep(gameId).transact[Task]
    }
  }

  override def insert(
      gameId: ID[Game],
      step: Int,
      state: State,
      move: Move,
      winner: Option[User]
  ): Task[Unit] = {
    lazy val activePlayer = state.players.activePlayer
    val query = for {
      lastStep <- findLastStep(gameId)
      _ <-
        if (step != lastStep + 1) rollback
        else Free.pure[ConnectionOp, Unit](())
      _ <- recordNextState(
        gameId,
        step,
        activePlayer.id
      )
      _ <- winner match {
        case Some(user) => recordWinner(gameId, user.id)
        case None       => Free.pure[ConnectionOp, Unit](())
      }
      _ <- recordPlayers(gameId, step, state.players.toList)
      _ <- move match {
        case PawnMove(_)             => Free.pure[ConnectionOp, Unit](())
        case PlaceWall(wallPosition) => recordWall(gameId, step, wallPosition)
      }
    } yield ()

    dataBase.transact(query.transact[Task])
  }

  override def create(gameId: ID[Game], state: State): Task[Game] = {
    lazy val activePlayer = state.players.activePlayer
    val step = 0
    val query = for {
      _ <- recordNextState(
        gameId,
        step,
        activePlayer.id
      )
      _ <- recordPlayers(gameId, step, state.players.toList)
    } yield Game(gameId, 0, state, None)

    dataBase.transact(query.transact[Task])
  }

  override def hasStarted(gameId: ID[Game]): Task[Boolean] = {
    dataBase
      .transact { transactor =>
        queries
          .hasStarted(gameId)
          .option
          .transact(transactor)
      }
      .map(_.nonEmpty)
  }

  override def history(id: ID[User]): Task[List[ID[Game]]] = {
    dataBase.transact {
      queries
        .findUserGames(id)
        .to[List]
        .transact[Task]
    }
  }

  override def findParticipants(gameId: ID[Game]): Task[GamePreView] = {
    val query = for {
      users <- findUsersByGameId(gameId)
      winner <- findWinnerByGameId(gameId)
    } yield GamePreView(gameId, users, winner)

    dataBase.transact(query.transact[Task])
  }

  private def findEnemiesByGameId(
      gameId: ID[Game],
      step: Int
  ): ConnectionIO[NonEmptyList[Player]] = {
    queries
      .findEnemiesByGameId(gameId, step)
      .to[List]
      .map {
        case List()  => throw GameNotFoundException(gameId)
        case x :: xs => NonEmptyList(x, xs)
      }
  }

  private def findActivePlayerByGameId(
      gameId: ID[Game],
      step: Int
  ): ConnectionIO[Player] =
    queries.findActivePlayerByGameId(gameId, step).unique

  private def findWallsByGameId(
      gameId: ID[Game],
      step: Int
  ): ConnectionIO[Set[WallPosition]] =
    queries.findWallsByGameId(gameId, step).to[Set]

  private def findWinnerByGameId(gameId: ID[Game]): ConnectionIO[Option[User]] =
    queries.findWinnerByGameId(gameId).option

  private def recordNextState(
      gameId: ID[Game],
      step: Int,
      activePlayerId: ID[User]
  ): ConnectionIO[Unit] = {
    queries
      .recordNextState(
        gameId,
        step: Int,
        activePlayerId
      )
      .run
      .map(_ => ())
      .exceptSomeSqlState { case FOREIGN_KEY_VIOLATION =>
        throw GameNotFoundException(gameId)
      }
  }

  private def recordWinner(
      gameId: ID[Game],
      userId: ID[User]
  ): ConnectionIO[Unit] = {
    queries
      .recordWinner(gameId, userId)
      .run
      .map(_ => ())
  }

  private def recordPlayers(
      gameId: ID[Game],
      step: Int,
      players: List[Player]
  ): ConnectionIO[Unit] = {
    queries.recordPlayers
      .updateMany(
        players.map { // (game_id, step, user_id, walls_amount, "row", "column")
          case Player(id, _, PawnPosition(row, column), wallsAmount, _) =>
            (gameId, step, id, wallsAmount, row, column)
        }
      )
      .map(_ => ())
      .exceptSomeSqlState { case FOREIGN_KEY_VIOLATION =>
        throw GameNotFoundException(gameId)
      }
  }

  private def recordWall(
      gameId: ID[Game],
      step: Int,
      wallPosition: WallPosition
  ): ConnectionIO[Int] = {
    queries
      .recordWall(
        gameId,
        step,
        wallPosition.orientation,
        wallPosition.row,
        wallPosition.column
      )
      .run
      .exceptSomeSqlState { case FOREIGN_KEY_VIOLATION =>
        throw GameNotFoundException(gameId)
      }
  }

  private def findUsersByGameId(gameId: ID[Game]): ConnectionIO[List[User]] = {
    queries
      .findUsersByGameId(gameId)
      .to[List]
      .map {
        case Nil => throw GameNotFoundException(gameId)
        case xs  => xs
      }
  }

  private def findLastStep(id: ID[Game]): ConnectionIO[Int] = {
    queries
      .lastStep(id)
      .unique
  }
}
