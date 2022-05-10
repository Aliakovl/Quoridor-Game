package model.storage.sqlStorage

import cats.data.NonEmptyList
import doobie.{ConnectionIO, Meta, Update}
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import model.GameException.{GameNotFoundException, LoginOccupiedException, SamePlayerException, UserNotFoundException}
import model.game.geometry.Side.North
import model.game.geometry.{Orientation, PawnPosition, Side, WallPosition}
import model.game.{Game, Player}
import model.{ProtoGame, ProtoPlayer, User}
import utils.Typed.Implicits._
import utils.Typed.ID

import java.util.UUID


object queries {

  implicit def MetaID[T]: Meta[ID[T]] = UuidType.imap(_.typed[T])(_.unType)

  def findUserById(userId: ID[User]): ConnectionIO[User] = {
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
    lazy val userId = UUID.randomUUID().typed[User]
    sql"""
    INSERT INTO "user" (id, login)
    VALUES ($userId, $login)
    """
      .update
      .run
      .exceptSomeSqlState{
        case UNIQUE_VIOLATION => throw LoginOccupiedException(login)
      }
      .map{ _ =>
        User(userId, login)
      }
  }

  def findProtoGameByGameId(gameId: ID[Game]): ConnectionIO[ProtoGame] = {
    sql"""
    SELECT "user".id, login, target
    FROM game
    JOIN player ON player.game_id = game.id
    JOIN "user" ON player.user_id = "user".id
    WHERE game.id = $gameId
    """
      .query[ProtoPlayer]
      .to[List]
      .map {
        case Seq() => throw GameNotFoundException(gameId)
        case protoPlayer => ProtoGame(gameId, NonEmptyList.fromListUnsafe(protoPlayer))
      }
  }

  def createProtoGameByUser(gameId: ID[Game], userId: ID[User]): ConnectionIO[Unit] = {
    val target = North
    sql"""
    INSERT INTO game (id)
    VALUES ($gameId);
    INSERT INTO player (game_id, user_id, target)
    VALUES ($gameId, $userId, ${Side.toEnum(target)}::side)
    """
      .update
      .run
      .map(_ => ())
  }

  def addUserIntoProtoGame(gameId: ID[Game], userId: ID[User], target: Side): ConnectionIO[Unit] = {
    sql"""
    INSERT INTO player (game_id, user_id, target)
    VALUES ($gameId, $userId, ${Side.toEnum(target)}::side)
    """
      .update
      .run
      .exceptSomeSqlState{
        case UNIQUE_VIOLATION => throw SamePlayerException(userId, gameId)
      }
      .map(_ => ())
  }

  def findPlayersByGameId(gameId: ID[Game]): ConnectionIO[List[Player]] = {
    sql"""
    SELECT
    pp.user_id,
    login,
    "row",
    "column",
    wallsAmount,
    target
    FROM pawn_position pp
    JOIN "user" u ON pp.user_id = u.id
    JOIN game_state gs ON gs.id = pp.game_state_id
    JOIN player p ON p.game_id = gs.game_id
    AND p.user_id = pp.user_id
    WHERE pp.game_state_id = $gameId
    """
      .query[Player]
      .to[List]
  }

  def findWallsByGameId(gameId: ID[Game]): ConnectionIO[Set[WallPosition]] = {
    sql"""
    SELECT
    orient,
    "row",
    "column"
    FROM wall_position
    WHERE game_state_id = $gameId
    """.query[WallPosition].to[Set]
  }

  def findActivePlayerByGameId(gameId: ID[Game]): ConnectionIO[Player] = {
    sql"""
    SELECT
    active_player,
    login,
    "row",
    "column",
    wallsAmount,
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

  def findEnemiesByGameId(gameId: ID[Game]): ConnectionIO[NonEmptyList[Player]] = {
    sql"""
    SELECT
    pp.user_id,
    login,
    "row",
    "column",
    wallsAmount,
    target
    FROM pawn_position pp
    JOIN "user" u ON pp.user_id = u.id
    JOIN game_state gs ON gs.id = pp.game_state_id
    JOIN player p ON p.game_id = gs.game_id
    AND p.user_id = pp.user_id
    WHERE pp.game_state_id = $gameId
    AND NOT pp.user_id = gs.active_player
    """.query[Player].to[List].map{
      case List() => throw GameNotFoundException(gameId)
      case x :: xs => NonEmptyList(x, xs)
    }
  }

  def previousGameId(gameId: ID[Game]): ConnectionIO[ID[Game]] = {
    sql"""
    SELECT
    previous_state
    FROM game_state
    WHERE id = $gameId
    """
      .query[ID[Game]]
      .option
      .map {
        case Some(v) => v
        case None    => throw GameNotFoundException(gameId)
      }
  }

  def findProtoGameIdByGameId(gameId: ID[Game]): ConnectionIO[ID[Game]] = {
    sql"""
    SELECT game_id FROM game_state
    WHERE id = $gameId
    """
      .query[ID[Game]]
      .option
      .map {
        case Some(v) => v
        case None    => throw GameNotFoundException(gameId)
      }
  }

  def recordNextState(gameId: ID[Game], previousGameId: ID[Game], protoGameId: ID[Game], activePlayerId: ID[User]): ConnectionIO[Unit] = {
    sql"""
    INSERT INTO game_state (id, game_id, previous_state, active_player)
    VALUES ($gameId, $protoGameId, $previousGameId, $activePlayerId);
    """.update.run.map(_ => ())
  }

  def recordPlayers(gameId: ID[Game], players: List[Player]): ConnectionIO[Unit] = {
    val sql = """
        INSERT INTO pawn_position (game_state_id, user_id, wallsamount, "row", "column")
        VALUES (?, ?, ?, ?, ?)
        """
    type PP = (ID[Game], ID[User], Int, Int, Int)

    Update[PP](sql).updateMany(players.map{ case Player(id, _, PawnPosition(row, column), wallsAmount, _) =>
      (gameId, id, wallsAmount, row, column)
    }).map(_ => ())
  }

  def recordWalls(gameId: ID[Game], wallPositions: Set[WallPosition]): ConnectionIO[Unit] = {
    val sql = """
        INSERT INTO wall_position (game_state_id, orient, "row", "column")
        VALUES (?, ?, ?, ?)
        """

    type PP = (ID[Game], Orientation, Int, Int)

    Update[PP](sql).updateMany(wallPositions.map{ case WallPosition(orientation, row, column) =>
      (gameId, orientation, row, column)
    }.toList).map(_ => ())
  }

  def findGameLeavesByUserId(userId: ID[User]): ConnectionIO[Set[ID[Game]]] = {
    sql"""
    SELECT
    g.id
    FROM game_state g
    LEFT JOIN game_state p
    ON g.id = p.previous_state
    JOIN player
    ON g.game_id = player.game_id
    WHERE p.id IS NULL
    AND player.user_id = $userId
    """.query[ID[Game]].to[Set]
  }

  def findGameBranchEndedOnGameId(gameId: ID[Game]): ConnectionIO[Set[ID[Game]]] = {
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
    """.query[ID[Game]].to[Set]
  }

}
