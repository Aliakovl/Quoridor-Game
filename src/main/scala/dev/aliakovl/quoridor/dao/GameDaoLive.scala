package dev.aliakovl.quoridor.dao

import dev.aliakovl.quoridor.dao.quill.QuillContext
import dev.aliakovl.quoridor.GameException.GameNotFoundException
import dev.aliakovl.quoridor.model.{Game, GamePreView, User}
import dev.aliakovl.utils.ZIOExtensions.*
import dev.aliakovl.utils.tagging.ID
import cats.data.NonEmptyList
import dev.aliakovl.quoridor.engine.game.{Move, Player, Players, State}
import dev.aliakovl.quoridor.engine.game.geometry.{PawnPosition, WallPosition}
import io.getquill.*
import org.postgresql.util.PSQLState
import zio.{IO, RLayer, Task, ZIO, ZLayer}

import java.sql.SQLException

class GameDaoLive(quillContext: QuillContext) extends GameDao:
  import quillContext.*
  import quillContext.given

  override def find(gameId: ID[Game]): Task[Game] = transaction {
    for {
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
  }

  override def find(gameId: ID[Game], step: Int): Task[Game] = transaction {
    for {
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
  }

  override def lastStep(gameId: ID[Game]): Task[Int] = {
    findLastStep(gameId)
  }

  override def insert(
      gameId: ID[Game],
      step: Int,
      state: State,
      move: Move,
      winner: Option[User]
  ): Task[Unit] = transaction {
    val activePlayer = state.players.activePlayer
    for {
      lastStep <- findLastStep(gameId)
      _ <- ZIO
        .succeed(step == lastStep + 1)
        .orFail(new Throwable("Filed transaction"))
      _ <- recordNextState(gameId, step, activePlayer.id)
      _ <- winner match {
        case Some(user) => recordWinner(gameId, user.id)
        case None       => ZIO.unit
      }
      _ <- recordPlayers(gameId, step, state.players.toList)
      _ <- move match {
        case Move.PawnMove(_) => ZIO.unit
        case Move.PlaceWall(wallPosition) =>
          recordWall(gameId, step, wallPosition)
      }
    } yield ()
  }

  override def create(gameId: ID[Game], state: State): Task[Game] =
    transaction {
      val activePlayer = state.players.activePlayer
      val step = 0
      for {
        _ <- recordNextState(
          gameId,
          step,
          activePlayer.id
        )
        _ <- recordPlayers(gameId, step, state.players.toList)
      } yield Game(gameId, 0, state, None)
    }

  override def history(userId: ID[User]): Task[List[ID[Game]]] = run {
    query[dto.Player].filter(_.userId == lift(userId)).map(_.gameId)
  }

  override def hasStarted(gameId: ID[Game]): Task[Boolean] = run {
    query[dto.GameState].filter { gameState =>
      gameState.gameId == lift(gameId) && gameState.step == 0
    }
  }.map(_.nonEmpty)

  override def findParticipants(gameId: ID[Game]): Task[GamePreView] = {
    for {
      users <- findUsersByGameId(gameId)
      winner <- findWinnerByGameId(gameId)
    } yield GamePreView(gameId, users, winner)
  }

  private def findLastStep(
      gameId: ID[Game]
  ): Task[Int] = {
    run(quote {
      query[dto.GameState]
        .filter(_.gameId == lift(gameId))
        .map(_.step)
        .max
    }).someOrFail(GameNotFoundException(gameId))
  }

  private def findEnemiesByGameId(
      gameId: ID[Game],
      step: Int
  ): Task[NonEmptyList[Player]] = {
    run(quote {
      for {
        player <- query[dto.Player]
        gameState <- query[dto.GameState].join(_.gameId == player.gameId)
        pawnPosition <- query[dto.PawnPosition].join { pawnPosition =>
          pawnPosition.gameId == gameState.gameId &&
          pawnPosition.step == gameState.step &&
          pawnPosition.userId == player.userId
        }
        if pawnPosition.gameId == lift(gameId) &&
          pawnPosition.step == lift(step) &&
          pawnPosition.userId != gameState.activePlayer
      } yield Player(
        player.userId,
        PawnPosition(pawnPosition.row, pawnPosition.column),
        pawnPosition.wallsAmount,
        player.target
      )
    }).flatMap {
      case Nil     => ZIO.fail(GameNotFoundException(gameId))
      case x :: xs => ZIO.succeed(NonEmptyList(x, xs))
    }
  }

  private def findActivePlayerByGameId(
      gameId: ID[Game],
      step: Int
  ): Task[Player] = {
    run(quote {
      for {
        player <- query[dto.Player]
        gameState <- query[dto.GameState].join(_.gameId == player.gameId)
        pawnPosition <- query[dto.PawnPosition].join { pawnPosition =>
          pawnPosition.gameId == gameState.gameId &&
          pawnPosition.step == gameState.step &&
          pawnPosition.userId == player.userId
        }
        if pawnPosition.gameId == lift(gameId) &&
          pawnPosition.step == lift(step) &&
          player.userId == gameState.activePlayer
      } yield Player(
        player.userId,
        PawnPosition(pawnPosition.row, pawnPosition.column),
        pawnPosition.wallsAmount,
        player.target
      )
    })
      .map(_.headOption)
      .someOrFail(GameNotFoundException(gameId))
  }

  private def findWallsByGameId(
      gameId: ID[Game],
      step: Int
  ): IO[SQLException, Set[WallPosition]] = {
    run(quote {
      query[dto.WallPosition]
        .filter { wallPosition =>
          wallPosition.gameId == lift(gameId) &&
          wallPosition.step <= lift(step)
        }
        .map { wallPosition =>
          WallPosition(
            wallPosition.orient,
            wallPosition.row,
            wallPosition.column
          )
        }
    }).map(_.toSet)
  }

  private def findWinnerByGameId(
      gameId: ID[Game]
  ): IO[SQLException, Option[User]] = {
    run(quote {
      for {
        winner <- query[dto.Winner]
        user <- query[dto.Userdata].join(_.userId == winner.userId)
        if winner.gameId == lift(gameId)
      } yield User(user.userId, user.username)
    }).map(_.headOption)
  }

  private def recordNextState(
      gameId: ID[Game],
      step: Int,
      activePlayerId: ID[User]
  ): Task[Unit] = {
    run(quote {
      query[dto.GameState].insert(
        _.gameId -> lift(gameId),
        _.step -> lift(step),
        _.activePlayer -> lift(activePlayerId)
      )
    }).unit
      .catchSome {
        case x: SQLException
            if x.getSQLState == PSQLState.FOREIGN_KEY_VIOLATION.getState =>
          ZIO.fail(GameNotFoundException(gameId))
      }
  }

  private def recordPlayers(
      gameId: ID[Game],
      step: Int,
      players: List[Player]
  ): Task[Unit] = {
    run(quote {
      liftQuery {
        players.map {
          case Player(userId, PawnPosition(row, column), wallsAmount, _) =>
            dto.PawnPosition(gameId, step, userId, wallsAmount, row, column)
        }
      }.foreach { pawnPosition =>
        query[dto.PawnPosition].insertValue(pawnPosition)
      }
    }).unit
      .catchSome {
        case x: SQLException
            if x.getSQLState == PSQLState.FOREIGN_KEY_VIOLATION.getState =>
          ZIO.fail(GameNotFoundException(gameId))
      }
  }

  private def recordWall(
      gameId: ID[Game],
      step: Int,
      wallPosition: WallPosition
  ): Task[Long] = {
    run(quote {
      query[dto.WallPosition].insert(
        _.gameId -> lift(gameId),
        _.step -> lift(step),
        _.orient -> lift(wallPosition.orientation),
        _.row -> lift(wallPosition.row),
        _.column -> lift(wallPosition.column)
      )
    })
      .catchSome {
        case x: SQLException
            if x.getSQLState == PSQLState.FOREIGN_KEY_VIOLATION.getState =>
          ZIO.fail(GameNotFoundException(gameId))
      }
  }

  private def findUsersByGameId(gameId: ID[Game]): Task[List[User]] = {
    run(quote {
      for {
        player <- query[dto.Player]
        user <- query[dto.Userdata].join(_.userId == player.userId)
        if player.gameId == lift(gameId)
      } yield User(user.userId, user.username)
    })
      .reject { case Nil =>
        GameNotFoundException(gameId)
      }
  }

  private def recordWinner(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[Unit] = {
    run(quote {
      query[dto.Winner].insert(
        _.userId -> lift(userId),
        _.gameId -> lift(gameId)
      )
    }).unit
  }

object GameDaoLive:
  val live: RLayer[QuillContext, GameDao] =
    ZLayer.fromFunction(new GameDaoLive(_))
