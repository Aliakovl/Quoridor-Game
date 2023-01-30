package ru.quoridor.storage.sqlStorage

import cats.effect.Resource
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

class GameStorageImpl(transactor: Resource[Task, Transactor[Task]])
    extends GameStorage {
  override def find(gameId: ID[Game]): Task[Game] = transactor.use { xa =>
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

    query.transact(xa)
  }

  override def insert(
      previousGameId: ID[Game],
      state: State,
      winner: Option[User]
  ): Task[Game] = transactor.use { xa =>
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

    query.transact(xa)
  }

  override def create(protoGameId: ID[Game], state: State): Task[Game] =
    transactor.use { xa =>
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

      query.transact(xa)
    }

  override def exists(gameId: ID[Game]): Task[Boolean] = transactor.use { xa =>
    queries
      .existsGameWithId(gameId)
      .transact(xa)
  }

  override def gameHistory(gameId: ID[Game]): Task[List[ID[Game]]] =
    transactor.use { xa =>
      queries
        .findGameBranchEndedOnGameId(gameId)
        .map(_.reverse)
        .transact(xa)
    }

  override def findParticipants(gameId: ID[Game]): Task[GamePreView] =
    transactor.use { xa =>
      val query = for {
        users <- queries.findUsersByGameId(gameId)
        winner <- queries.findWinnerByGameId(gameId)
      } yield quoridor.GamePreView(gameId, users, winner)

      query.transact(xa)
    }
}

object GameStorageImpl {
  def apply(transactor: Resource[Task, Transactor[Task]]): GameStorageImpl =
    new GameStorageImpl(transactor)
}
