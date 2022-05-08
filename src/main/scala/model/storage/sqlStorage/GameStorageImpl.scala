package model.storage.sqlStorage

import cats.effect.Async
import doobie.implicits._
import doobie.util.transactor.Transactor
import model.game.{Game, GameState, Player}
import model.storage.GameStorage
import java.util.UUID


class GameStorageImpl[F[_]: Async](implicit xa: Transactor[F]) extends GameStorage[F] {
  override def find(gameId: UUID): F[Game] = {
    val query = for {
      _ <- queries.previousGameId(gameId) // throw exception if there is no gameId
      activePlayer <- queries.activePlayerByGameId(gameId)
      players <- queries.findPlayersByGameId(gameId)
      walls <- queries.findWallsByGameId(gameId)
    } yield Game(gameId, activePlayer, GameState(players, walls))

    query.transact(xa)
  }

  override def insert(previousGameId: UUID, activePlayer: Player, state: GameState): F[Game] = {
    lazy val gameId = UUID.randomUUID()
    val query = for {
      protoGameId <- queries.protoGameId(previousGameId)
      _ <- queries.recordNextState(gameId, previousGameId, protoGameId, activePlayer.id)
      _ <- queries.recordPlayers(gameId, state.players)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, activePlayer, state)

    query.transact(xa)
  }

  override def create(protoGameId: UUID, activePlayer: Player, state: GameState): F[Game] = {
    val gameId = protoGameId
    val query = for {
      _ <- queries.recordNextState(gameId, protoGameId, protoGameId, activePlayer.id)
      _ <- queries.recordPlayers(gameId, state.players)
      _ <- queries.recordWalls(gameId, state.walls)
    } yield Game(gameId, activePlayer, state)

    query.transact(xa)
  }
}