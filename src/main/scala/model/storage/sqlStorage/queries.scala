package model.storage.sqlStorage

import doobie.Update
import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import model.GameException.{GameNotFoundException, LoginOccupiedException, SamePlayerException, UserNotFoundException}
import model.game.geometry.Side.North
import model.game.geometry.{Orientation, PawnPosition, Side, WallPosition}
import model.game.Player
import model.{ProtoGame, ProtoPlayer, User}
import java.util.UUID


object queries {
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

  def findProtoGameById(gameId: UUID): ConnectionIO[ProtoGame] = {
    sql"""
    SELECT "user".id, login, target
    FROM game
    JOIN player ON player.game_id = game.id
    JOIN "user" ON player.user_id = "user".id
    WHERE game.id = $gameId
    """
      .query[ProtoPlayer]
      .to[Seq]
      .map {
        case Seq() => throw GameNotFoundException(gameId)
        case protoPlayer => ProtoGame(gameId, protoPlayer)
      }
  }

  def createProtoGameByUser(gameId: UUID, userId: UUID): ConnectionIO[Unit] = {
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

  def addUserIntoProtoGame(gameId: UUID, userId: UUID, target: Side): ConnectionIO[Unit] = {
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

  def findPlayersByGameId(gameId: UUID): ConnectionIO[Set[Player]] = {
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
    """.query[Player].to[Set]
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

  def activePlayerByGameId(gameId: UUID): ConnectionIO[Player] = {
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

  def protoGameId(gameId: UUID): ConnectionIO[UUID] = {
    println(gameId)
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

  def recordNextState(gameId: UUID, previousGameId: UUID, protoGameId: UUID, activePlayerId: UUID): ConnectionIO[Unit] = {
    sql"""
    INSERT INTO game_state (id, game_id, previous_state, active_player)
    VALUES ($gameId, $protoGameId, $previousGameId, $activePlayerId);
    """.update.run.map(_ => ())
  }

  def recordPlayers(gameId: UUID, players: Set[Player]): ConnectionIO[Unit] = {

    val sql = """
        INSERT INTO pawn_position (game_state_id, user_id, wallsamount, "row", "column")
        VALUES (?, ?, ?, ?, ?)
        """
    type PP = (UUID, UUID, Int, Int, Int)

    Update[PP](sql).updateMany(players.map{ case Player(id, _, PawnPosition(row, column), wallsAmount, _) =>
      (gameId, id, wallsAmount, row, column)
    }.toList).map(_ => ())
  }

  def recordWalls(gameId: UUID, wallPositions: Set[WallPosition]): ConnectionIO[Unit] = {
    val sql = """
        INSERT INTO wall_position (game_state_id, orient, "row", "column")
        VALUES (?, ?, ?, ?)
        """

    type PP = (UUID, Orientation, Int, Int)

    Update[PP](sql).updateMany(wallPositions.map{ case WallPosition(orientation, row, column) =>
      (gameId, orientation, row, column)
    }.toList).map(_ => ())
  }

}
