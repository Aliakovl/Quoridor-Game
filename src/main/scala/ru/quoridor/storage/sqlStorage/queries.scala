package ru.quoridor.storage.sqlStorage

import cats.data.NonEmptyList
import doobie.{ConnectionIO, Update}
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.postgres.sqlstate.class23.{
  FOREIGN_KEY_VIOLATION,
  UNIQUE_VIOLATION
}
import ru.quoridor.model.GameException._
import ru.quoridor.model.{ProtoGame, ProtoPlayer, ProtoPlayers, User}
import ru.quoridor.model.game.geometry.Side.North
import ru.quoridor.model.game.Player
import ru.quoridor.model.game.geometry.{
  Orientation,
  PawnPosition,
  Side,
  WallPosition
}

import java.util.UUID

object queries {

  def findUserByLogin(login: String): ConnectionIO[User] = {
    sql"""
    SELECT * FROM "user"
    WHERE login = $login
    """
      .query[User]
      .option
      .map {
        case Some(v) => v
        case None    => throw LoginNotFoundException(login)
      }
  }

  def findUserById(userId: UUID): ConnectionIO[User] = {
    sql"""
    SELECT * FROM "user"
    WHERE id = $userId
    """
      .query[User]
      .option
      .map {
        case Some(v) => v
        case None    => throw UserNotFoundException(userId)
      }
  }

  def registerUser(login: String): ConnectionIO[User] = {
    lazy val userId = UUID.randomUUID()
    sql"""
    INSERT INTO "user" (id, login)
    VALUES ($userId, $login)
    """.update.run
      .exceptSomeSqlState { case UNIQUE_VIOLATION =>
        throw LoginOccupiedException(login)
      }
      .map { _ =>
        User(userId, login)
      }
  }

  def findProtoGameByGameId(gameId: UUID): ConnectionIO[ProtoGame] = {
    sql"""
    SELECT "user".id, login, target
    FROM game
    JOIN player ON player.game_id = game.id
    JOIN "user" ON player.user_id = "user".id
    WHERE game.id = $gameId
    ORDER BY user_id = game.creator DESC
    """
      .query[ProtoPlayer]
      .to[List]
      .map {
        case Nil => throw GameNotFoundException(gameId)
        case creator :: guests =>
          ProtoGame(gameId, ProtoPlayers(creator, guests))
      }
  }

  def createProtoGameByUser(
      gameId: UUID,
      userId: UUID
  ): ConnectionIO[Unit] = {
    val target = North
    sql"""
    INSERT INTO game (id, creator)
    VALUES ($gameId, $userId);
    INSERT INTO player (game_id, user_id, target)
    VALUES ($gameId, $userId, ${Side.toEnum(target)}::side)
    """.update.run
      .map(_ => ())
  }

  def addUserIntoProtoGame(
      gameId: UUID,
      userId: UUID,
      target: Side
  ): ConnectionIO[Unit] = {
    sql"""
    INSERT INTO player (game_id, user_id, target)
    VALUES ($gameId, $userId, ${Side.toEnum(target)}::side)
    """.update.run
      .exceptSomeSqlState {
        case UNIQUE_VIOLATION      => throw SamePlayerException(userId, gameId)
        case FOREIGN_KEY_VIOLATION => throw GameNotFoundException(gameId)
      }
      .map(_ => ())
  }

  def findWallsByGameId(gameId: UUID): ConnectionIO[Set[WallPosition]] = {
    sql"""
    SELECT
    orient,
    "row",
    "column"
    FROM wall_position
    WHERE game_state_id = $gameId
    """.query[WallPosition].to[Set]
  }

  def findActivePlayerByGameId(gameId: UUID): ConnectionIO[Player] = {
    sql"""
    SELECT
    active_player,
    login,
    "row",
    "column",
    walls_amount,
    target
    from game_state
    JOIN "user" ON active_player = "user".id
    JOIN pawn_position pp ON pp.game_state_id = game_state.id
    AND pp.user_id = active_player
    JOIN player p ON p.game_id = game_state.game_id
    AND p.user_id = active_player
    WHERE game_state.id = $gameId
    """.query[Player].unique
  }

  def findEnemiesByGameId(
      gameId: UUID
  ): ConnectionIO[NonEmptyList[Player]] = {
    sql"""
    SELECT
    pp.user_id,
    login,
    "row",
    "column",
    walls_amount,
    target
    FROM pawn_position pp
    JOIN "user" u ON pp.user_id = u.id
    JOIN game_state gs ON gs.id = pp.game_state_id
    JOIN player p ON p.game_id = gs.game_id
    AND p.user_id = pp.user_id
    WHERE pp.game_state_id = $gameId
    AND NOT pp.user_id = gs.active_player
    """.query[Player].to[List].map {
      case List()  => throw GameNotFoundException(gameId)
      case x :: xs => NonEmptyList(x, xs)
    }
  }

  def previousGameId(gameId: UUID): ConnectionIO[UUID] = {
    sql"""
    SELECT
    previous_state
    FROM game_state
    WHERE id = $gameId
    """
      .query[UUID]
      .option
      .map {
        case Some(v) => v
        case None    => throw GameNotFoundException(gameId)
      }
  }

  def findProtoGameIdByGameId(gameId: UUID): ConnectionIO[UUID] = {
    sql"""
    SELECT game_id FROM game_state
    WHERE id = $gameId
    """
      .query[UUID]
      .option
      .map {
        case Some(v) => v
        case None    => throw GameNotFoundException(gameId)
      }
  }

  def recordNextState(
      gameId: UUID,
      previousGameId: UUID,
      protoGameId: UUID,
      activePlayerId: UUID,
      winner: Option[UUID]
  ): ConnectionIO[Unit] = {
    sql"""
    INSERT INTO game_state (id, game_id, previous_state, active_player, winner)
    VALUES ($gameId, $protoGameId, $previousGameId, $activePlayerId, $winner);
    """.update.run
      .map(_ => ())
      .exceptSomeSqlState { case FOREIGN_KEY_VIOLATION =>
        throw GameNotFoundException(gameId)
      }
  }

  def recordPlayers(
      gameId: UUID,
      players: List[Player]
  ): ConnectionIO[Unit] = {
    val sql = """
        INSERT INTO pawn_position (game_state_id, user_id, walls_amount, "row", "column")
        VALUES (?, ?, ?, ?, ?)
        """
    type PP = (UUID, UUID, Int, Int, Int)

    Update[PP](sql)
      .updateMany(players.map {
        case Player(userId, _, PawnPosition(row, column), wallsAmount, _) =>
          (gameId, userId, wallsAmount, row, column)
      })
      .map(_ => ())
      .exceptSomeSqlState { case FOREIGN_KEY_VIOLATION =>
        throw GameNotFoundException(gameId)
      }
  }

  def recordWalls(
      gameId: UUID,
      wallPositions: Set[WallPosition]
  ): ConnectionIO[Unit] = {
    val sql = """
        INSERT INTO wall_position (game_state_id, orient, "row", "column")
        VALUES (?, ?, ?, ?)
        """

    type PP = (UUID, Orientation, Int, Int)

    Update[PP](sql)
      .updateMany(wallPositions.map {
        case WallPosition(orientation, row, column) =>
          (gameId, orientation, row, column)
      }.toList)
      .map(_ => ())
      .exceptSomeSqlState { case FOREIGN_KEY_VIOLATION =>
        throw GameNotFoundException(gameId)
      }
  }

  def findGameLeavesByUserId(userId: UUID): ConnectionIO[List[UUID]] = {
    sql"""
    SELECT
    g.id
    FROM game_state g
    LEFT JOIN game_state p
    ON g.id = p.previous_state
    JOIN player
    ON g.game_id = player.game_id
    AND player.user_id = $userId
    WHERE p.id IS NULL OR g.id = g.game_id
    GROUP BY g.id
	  HAVING sum(1) = 1
    """.query[UUID].to[List]
  }

  def findGameBranchEndedOnGameId(
      gameId: UUID
  ): ConnectionIO[List[UUID]] = {
    sql"""
    WITH RECURSIVE game_branch AS (
    SELECT gs.id, gs.previous_state
    FROM game_state gs
    WHERE gs.id = $gameId
    UNION ALL
    SELECT gs.id, gs.previous_state FROM game_state gs
    JOIN game_branch
    ON gs.id = game_branch.previous_state
    WHERE NOT game_branch.id = game_branch.previous_state
    )

    SELECT id FROM game_branch
    """
      .query[UUID]
      .to[List]
      .map {
        case Nil => throw GameNotFoundException(gameId)
        case xs  => xs
      }
  }

  def existsGameWithId(gameId: UUID): ConnectionIO[Boolean] = {
    sql"""
    SELECT * FROM game_state
    WHERE id = $gameId
    """.query.option.map(_.nonEmpty)
  }

  def findWinnerByGameId(gameId: UUID): ConnectionIO[Option[User]] = {
    sql"""
    SELECT
    "user".id,
    "user".login
    FROM game_state
    JOIN "user"
    ON "user".id = winner
    WHERE game_state.id = $gameId
    """.query[User].option
  }

  def findUsersByGameId(gameId: UUID): ConnectionIO[List[User]] = {
    sql"""
    SELECT
    "user".id,
    "user".login
    FROM player
    JOIN "user"
    ON player.user_id = "user".id
    JOIN game_state gs
    ON player.game_id = gs.game_id
    JOIN game g
    ON gs.game_id = g.id
    WHERE gs.id = $gameId
    """
      .query[User]
      .to[List]
      .map {
        case Nil => throw GameNotFoundException(gameId)
        case xs  => xs
      }
  }
}
