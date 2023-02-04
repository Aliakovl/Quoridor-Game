package ru.quoridor.storage.sqlStorage

import doobie.Transactor
import doobie.implicits._
import ru.quoridor
import ru.quoridor.{GamePreView, User, game}
import ru.quoridor.game.{Game, State}
import ru.quoridor.storage.GameStorage
import ru.utils.Typed.Implicits._
import ru.utils.Typed.ID
import zio.Task
import zio.interop.catz._

import java.util.UUID

class GameStorageImpl(transactor: Transactor[Task]) extends GameStorage {
  override def find(gameId: ID[Game]): Task[Game] = {
    val query = for {
      _ <- queries.previousGameId(gameId)
      activePlayer <- queries.findActivePlayerByGameId(gameId)
      enemies <- queries.findEnemiesByGameId(gameId)
      walls <- queries.findWallsByGameId(gameId)
      winner <- queries.findWinnerByGameId(gameId)
    } yield game.Game(
      gameId,
      State(game.Players(activePlayer, enemies), walls),
      winner
    )

    query.transact(transactor)
  }

  override def insert(
      previousGameId: ID[Game],
      state: State,
      winner: Option[User]
  ): Task[Game] = {
    lazy val activePlayer = state.players.activePlayer
    lazy val gameId = UUID.randomUUID().typed[Game]
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

    query.transact(transactor)
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

    query.transact(transactor)
  }

  override def exists(gameId: ID[Game]): Task[Boolean] =
    queries
      .existsGameWithId(gameId)
      .transact(transactor)

  override def gameHistory(gameId: ID[Game]): Task[List[ID[Game]]] =
    queries
      .findGameBranchEndedOnGameId(gameId)
      .map(_.reverse)
      .transact(transactor)

  override def findParticipants(gameId: ID[Game]): Task[GamePreView] = {
    val query = for {
      users <- queries.findUsersByGameId(gameId)
      winner <- queries.findWinnerByGameId(gameId)
    } yield quoridor.GamePreView(gameId, users, winner)

    query.transact(transactor)
  }
}

object GameStorageImpl {
  def apply(transactor: Transactor[Task]): GameStorageImpl =
    new GameStorageImpl(transactor)
}
