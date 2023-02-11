package ru.quoridor.storage.sqlStorage

import cats.data.NonEmptyList
import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.sqlstate.class23.FOREIGN_KEY_VIOLATION
import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model.GameException.GameNotFoundException
import ru.quoridor.model.game.geometry.{PawnPosition, WallPosition}
import ru.quoridor.model.game.{Game, Player, Players, State}
import ru.quoridor.storage.{DataBase, GameStorage}
import ru.utils.tagging.ID
import zio.Task
import zio.interop.catz._

class GameStorageImpl(dataBase: DataBase) extends GameStorage {
  override def find(gameId: ID[Game]): Task[Game] = {
    val query = for {
      enemies <- findEnemiesByGameId(gameId)
      activePlayer <- findActivePlayerByGameId(gameId)
      walls <- findWallsByGameId(gameId)
      winner <- findWinnerByGameId(gameId)
    } yield Game(
      gameId,
      State(Players(activePlayer, enemies), walls),
      winner
    )

    dataBase.transact(query.transact[Task])
  }

  override def insert(
      gameId: ID[Game],
      previousGameId: ID[Game],
      state: State,
      winner: Option[User]
  ): Task[Game] = {
    lazy val activePlayer = state.players.activePlayer
    val query = for {
      protoGameId <- findProtoGameIdByGameId(previousGameId)
      _ <- recordNextState(
        gameId,
        previousGameId,
        protoGameId,
        activePlayer.id,
        winner.map(_.id)
      )
      _ <- recordPlayers(gameId, state.players.toList)
      _ <- recordWalls(gameId, state.walls)
    } yield Game(gameId, state, winner)

    dataBase.transact(query.transact[Task])
  }

  override def create(protoGameId: ID[Game], state: State): Task[Game] = {
    lazy val activePlayer = state.players.activePlayer
    val gameId = protoGameId
    val query = for {
      _ <- recordNextState(
        gameId,
        protoGameId,
        protoGameId,
        activePlayer.id,
        None
      )
      _ <- recordPlayers(gameId, state.players.toList)
      _ <- recordWalls(gameId, state.walls)
    } yield Game(gameId, state, None)

    dataBase.transact(query.transact[Task])
  }

  override def exists(gameId: ID[Game]): Task[Boolean] = dataBase
    .transact { transactor =>
      queries
        .existsGameWithId(gameId)
        .option
        .transact(transactor)
    }
    .map(_.nonEmpty)

  override def gameHistory(gameId: ID[Game]): Task[List[ID[Game]]] =
    dataBase
      .transact { transactor =>
        queries
          .findGameBranchEndedOnGameId(gameId)
          .to[List]
          .transact(transactor)
      }
      .map {
        case Nil => throw GameNotFoundException(gameId)
        case xs  => xs.reverse
      }

  override def findParticipants(gameId: ID[Game]): Task[GamePreView] = {
    val query = for {
      users <- findUsersByGameId(gameId)
      winner <- findWinnerByGameId(gameId)
    } yield GamePreView(gameId, users, winner)

    dataBase.transact(query.transact[Task])
  }

  private def findEnemiesByGameId(
      gameId: ID[Game]
  ): ConnectionIO[NonEmptyList[Player]] = {
    queries
      .findEnemiesByGameId(gameId)
      .to[List]
      .map {
        case List()  => throw GameNotFoundException(gameId)
        case x :: xs => NonEmptyList(x, xs)
      }
  }

  private def findActivePlayerByGameId(gameId: ID[Game]): ConnectionIO[Player] =
    queries.findActivePlayerByGameId(gameId).unique

  private def findWallsByGameId(
      gameId: ID[Game]
  ): ConnectionIO[Set[WallPosition]] =
    queries.findWallsByGameId(gameId).to[Set]

  private def findWinnerByGameId(gameId: ID[Game]): ConnectionIO[Option[User]] =
    queries.findWinnerByGameId(gameId).option

  private def findProtoGameIdByGameId(
      gameId: ID[Game]
  ): ConnectionIO[ID[Game]] =
    queries
      .findProtoGameIdByGameId(gameId)
      .option
      .map {
        case Some(v) => v
        case None    => throw GameNotFoundException(gameId)
      }

  private def recordNextState(
      gameId: ID[Game],
      previousGameId: ID[Game],
      protoGameId: ID[Game],
      activePlayerId: ID[User],
      winner: Option[ID[User]]
  ): ConnectionIO[Unit] = {
    queries
      .recordNextState(
        gameId,
        previousGameId,
        protoGameId,
        activePlayerId,
        winner
      )
      .run
      .map(_ => ())
      .exceptSomeSqlState { case FOREIGN_KEY_VIOLATION =>
        throw GameNotFoundException(gameId)
      }
  }

  private def recordPlayers(gameId: ID[Game], players: List[Player]) = {
    queries.recordPlayers
      .updateMany(players.map {
        case Player(id, _, PawnPosition(row, column), wallsAmount, _) =>
          (gameId, id, wallsAmount, row, column)
      })
      .map(_ => ())
      .exceptSomeSqlState { case FOREIGN_KEY_VIOLATION =>
        throw GameNotFoundException(gameId)
      }
  }

  private def recordWalls(
      gameId: ID[Game],
      wallPositions: Set[WallPosition]
  ): ConnectionIO[Unit] = {
    queries.recordWalls
      .updateMany(wallPositions.map {
        case WallPosition(orientation, row, column) =>
          (gameId, orientation, row, column)
      }.toList)
      .map(_ => ())
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
}
