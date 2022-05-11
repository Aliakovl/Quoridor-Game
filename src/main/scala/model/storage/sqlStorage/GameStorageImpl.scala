package model.storage.sqlStorage

import cats.effect.Async
import doobie.implicits._
import doobie.util.transactor.Transactor
import model.game.{Game, State, Players}
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
    } yield Game(gameId, State(Players(activePlayer, enemies), walls))

    query.transact(xa)
  }

  override def insert(previousGameId: ID[Game], state: State): F[Game] = {
    lazy val activePlayer = state.players.activePlayer
    lazy val gameId = UUID.randomUUID().typed[Game]
    val query = for {
      protoGameId <- queries.findProtoGameIdByGameId(previousGameId)
      _ <- queries.recordNextState(gameId, previousGameId, protoGameId, activePlayer.id)
      _ <- queries.recordPlayers(gameId, state.players.toList)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, state)

    query.transact(xa)
  }

  override def create(protoGameId: ID[Game], state: State): F[Game] = {
    lazy val activePlayer = state.players.activePlayer
    val gameId = protoGameId
    val query = for {
      _ <- queries.recordNextState(gameId, protoGameId, protoGameId, activePlayer.id)
      _ <- queries.recordPlayers(gameId, state.players.toList)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, state)

    query.transact(xa)
  }
}