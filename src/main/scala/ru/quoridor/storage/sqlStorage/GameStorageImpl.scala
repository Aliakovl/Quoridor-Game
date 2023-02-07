package ru.quoridor.storage.sqlStorage

import doobie.implicits._
import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model
import ru.quoridor.model.game.{Game, Players, State}
import ru.quoridor.storage.{DataBase, GameStorage}
import ru.utils.Tagged.Implicits._
import ru.utils.Tagged.ID
import zio.Task
import zio.interop.catz._

import java.util.UUID

class GameStorageImpl(dataBase: DataBase) extends GameStorage {
  override def find(gameId: ID[Game]): Task[Game] = {
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
      previousGameId: ID[Game],
      state: State,
      winner: Option[User]
  ): Task[Game] = {
    lazy val activePlayer = state.players.activePlayer
    lazy val gameId = UUID.randomUUID().tag[Game]
    val query = for {
      protoGameId <- queries.findProtoGameIdByGameId(previousGameId)
      _ <- queries.recordNextState(
        gameId,
        previousGameId,
        protoGameId,
        activePlayer.id,
        winner.map(_.id)
      )
      _ <- queries.recordPlayers(gameId, state.players.toList)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, state, winner)

    dataBase.transact(query.transact[Task])
  }

  override def create(protoGameId: ID[Game], state: State): Task[Game] = {
    lazy val activePlayer = state.players.activePlayer
    val gameId = protoGameId
    val query = for {
      _ <- queries.recordNextState(
        gameId,
        protoGameId,
        protoGameId,
        activePlayer.id,
        None
      )
      _ <- queries.recordPlayers(gameId, state.players.toList)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, state, None)

    dataBase.transact(query.transact[Task])
  }

  override def exists(gameId: ID[Game]): Task[Boolean] = dataBase.transact {
    transactor =>
      queries
        .existsGameWithId(gameId)
        .transact(transactor)
  }

  override def gameHistory(gameId: ID[Game]): Task[List[ID[Game]]] =
    dataBase.transact { transactor =>
      queries
        .findGameBranchEndedOnGameId(gameId)
        .map(_.reverse)
        .transact(transactor)
    }

  override def findParticipants(gameId: ID[Game]): Task[GamePreView] = {
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
