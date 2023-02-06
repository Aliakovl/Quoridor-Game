package ru.quoridor.storage.sqlStorage

import doobie.implicits._
import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model
import ru.quoridor.model.game.{Game, Players, State}
import ru.quoridor.storage.{DataBase, GameStorage}
import zio.Task
import zio.interop.catz._

import java.util.UUID

class GameStorageImpl(dataBase: DataBase) extends GameStorage {
  override def find(gameId: UUID): Task[Game] = {
    val query = for {
      _ <- queries.previousGameId(gameId)
      activePlayer <- queries.findActivePlayerByGameId(gameId)
      enemies <- queries.findEnemiesByGameId(gameId)
      walls <- queries.findWallsByGameId(gameId)
      winner <- queries.findWinnerByGameId(gameId)
    } yield Game(
      gameId,
      model.game.State(Players(activePlayer, enemies), walls),
      winner
    )

    dataBase.transact(query.transact[Task])
  }

  override def insert(
      previousGameId: UUID,
      state: State,
      winner: Option[User]
  ): Task[Game] = {
    lazy val activePlayer = state.players.activePlayer
    lazy val gameId = UUID.randomUUID()
    val query = for {
      protoGameId <- queries.findProtoGameIdByGameId(previousGameId)
      _ <- queries.recordNextState(
        gameId,
        previousGameId,
        protoGameId,
        activePlayer.userId,
        winner.map(_.userId)
      )
      _ <- queries.recordPlayers(gameId, state.players.toList)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, state, winner)

    dataBase.transact(query.transact[Task])
  }

  override def create(protoGameId: UUID, state: State): Task[Game] = {
    lazy val activePlayer = state.players.activePlayer
    val gameId = protoGameId
    val query = for {
      _ <- queries.recordNextState(
        gameId,
        protoGameId,
        protoGameId,
        activePlayer.userId,
        None
      )
      _ <- queries.recordPlayers(gameId, state.players.toList)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, state, None)

    dataBase.transact(query.transact[Task])
  }

  override def exists(gameId: UUID): Task[Boolean] = dataBase.transact {
    transactor =>
      queries
        .existsGameWithId(gameId)
        .transact(transactor)
  }

  override def gameHistory(gameId: UUID): Task[List[UUID]] =
    dataBase.transact { transactor =>
      queries
        .findGameBranchEndedOnGameId(gameId)
        .map(_.reverse)
        .transact(transactor)
    }

  override def findParticipants(gameId: UUID): Task[GamePreView] = {
    val query = for {
      users <- queries.findUsersByGameId(gameId)
      winner <- queries.findWinnerByGameId(gameId)
    } yield GamePreView(gameId, users, winner)

    dataBase.transact(query.transact[Task])
  }
}

object GameStorageImpl {
  def apply(dataBase: DataBase): GameStorageImpl =
    new GameStorageImpl(dataBase)
}
