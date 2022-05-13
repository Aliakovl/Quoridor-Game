package model.storage.sqlStorage

import cats.effect.Async
import doobie.implicits._
import doobie.util.transactor.Transactor
import model.{GamePreView, User}
import model.game.{Game, Players, State}
import model.storage.GameStorage
import utils.Typed.ID
import utils.Typed.Implicits._

import java.util.UUID


class GameStorageImpl[F[_]: Async](implicit xa: Transactor[F]) extends GameStorage[F] {
  override def find(gameId: ID[Game]): F[Game] = {
    val query = for {
      _ <- queries.previousGameId(gameId) // throw exception if there is no gameId
      activePlayer <- queries.findActivePlayerByGameId(gameId)
      enemies <- queries.findEnemiesByGameId(gameId)
      walls <- queries.findWallsByGameId(gameId)
      winner <- queries.findWinnerByGameId(gameId)
    } yield Game(gameId, State(Players(activePlayer, enemies), walls), winner)

    query.transact(xa)
  }

  override def insert(previousGameId: ID[Game], state: State, winner: Option[User]): F[Game] = {
    lazy val activePlayer = state.players.activePlayer
    lazy val gameId = UUID.randomUUID().typed[Game]
    val query = for {
      protoGameId <- queries.findProtoGameIdByGameId(previousGameId)
      _ <- queries.recordNextState(gameId, previousGameId, protoGameId, activePlayer.id, winner.map(_.id))
      _ <- queries.recordPlayers(gameId, state.players.toList)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, state, winner)

    query.transact(xa)
  }

  override def create(protoGameId: ID[Game], state: State): F[Game] = {
    lazy val activePlayer = state.players.activePlayer
    val gameId = protoGameId
    val query = for {
      _ <- queries.recordNextState(gameId, protoGameId, protoGameId, activePlayer.id, None)
      _ <- queries.recordPlayers(gameId, state.players.toList)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, state, None)

    query.transact(xa)
  }

  override def exists(gameId: ID[Game]): F[Boolean] = {
    queries.existsGameWithId(gameId)
      .transact(xa)
  }

  override def gameHistory(gameId: ID[Game]): F[List[ID[Game]]] = {
    queries.findGameBranchEndedOnGameId(gameId)
      .map(_.reverse)
      .transact(xa)
  }

  override def findParticipants(gameId: ID[Game]): F[GamePreView] = {
    val query = for {
      users <- queries.findUsersByGameId(gameId)
      winner <- queries.findWinnerByGameId(gameId)
    } yield GamePreView(gameId, users, winner)

    query.transact(xa)
  }
}