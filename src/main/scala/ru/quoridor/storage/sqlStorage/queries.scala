package ru.quoridor.storage.sqlStorage

import doobie.{Meta, Update, Update0}
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.query.Query0
import ru.quoridor.model.{ProtoPlayer, User}
import ru.quoridor.model.game.geometry.Side.North
import ru.quoridor.model.game.{Game, Player}
import ru.quoridor.model.game.geometry.{Orientation, Side, WallPosition}
import ru.utils.tagging.Tagged.Implicits._
import ru.utils.tagging.ID

object queries {

  implicit def MetaID[T]: Meta[ID[T]] = UuidType.imap(_.tag[T])(_.untag)

  def findUserByLogin(login: String): Query0[User] = {
    sql"""
    SELECT * FROM "user"
    WHERE login = $login
    """.query[User]
  }

  def findUserById(userId: ID[User]): Query0[User] = {
    sql"""
    SELECT * FROM "user"
    WHERE id = $userId
    """.query[User]
  }

  def registerUser(user: User): Update0 = {
    user match {
      case User(id, login) =>
        sql"""
        INSERT INTO "user" (id, login)
        VALUES ($id, $login)
        """.update
    }
  }

  def findProtoGameByGameId(gameId: ID[Game]): Query0[ProtoPlayer] = {
    sql"""
    SELECT "user".id, login, target
    FROM game
    JOIN player ON player.game_id = game.id
    JOIN "user" ON player.user_id = "user".id
    WHERE game.id = $gameId
    ORDER BY user_id = game.creator DESC
    """.query[ProtoPlayer]
  }

  def createProtoGameByUser(
      gameId: ID[Game],
      userId: ID[User]
  ): Update0 = {
    val target = North
    sql"""
    INSERT INTO game (id, creator)
    VALUES ($gameId, $userId);
    INSERT INTO player (game_id, user_id, target)
    VALUES ($gameId, $userId, ${Side.toEnum(target)}::side)
    """.update
  }

  def addUserIntoProtoGame(
      gameId: ID[Game],
      userId: ID[User],
      target: Side
  ): Update0 = {
    sql"""
    INSERT INTO player (game_id, user_id, target)
    VALUES ($gameId, $userId, ${Side.toEnum(target)}::side)
    """.update
  }

  def findWallsByGameId(gameId: ID[Game]): Query0[WallPosition] = {
    sql"""
    SELECT
    orient,
    "row",
    "column"
    FROM wall_position
    WHERE game_state_id = $gameId
    """.query[WallPosition]
  }

  def findActivePlayerByGameId(gameId: ID[Game]): Query0[Player] = {
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
    """.query[Player]
  }

  def findEnemiesByGameId(
      gameId: ID[Game]
  ): Query0[Player] = {
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
    """.query[Player]
  }

  def findProtoGameIdByGameId(gameId: ID[Game]): Query0[ID[Game]] = {
    sql"""
    SELECT game_id FROM game_state
    WHERE id = $gameId
    """.query[ID[Game]]
  }

  def recordNextState(
      gameId: ID[Game],
      previousGameId: ID[Game],
      protoGameId: ID[Game],
      activePlayerId: ID[User],
      winner: Option[ID[User]]
  ): Update0 = {
    sql"""
    INSERT INTO game_state (id, game_id, previous_state, active_player, winner)
    VALUES ($gameId, $protoGameId, $previousGameId, $activePlayerId, $winner);
    """.update
  }

  val recordPlayers: Update[(ID[Game], ID[User], Int, Int, Int)] = {
    val sql = """
        INSERT INTO pawn_position (game_state_id, user_id, walls_amount, "row", "column")
        VALUES (?, ?, ?, ?, ?)
        """
    type PP = (ID[Game], ID[User], Int, Int, Int)

    Update[PP](sql)
  }

  val recordWalls: Update[(ID[Game], Orientation, Int, Int)] = {
    val sql = """
        INSERT INTO wall_position (game_state_id, orient, "row", "column")
        VALUES (?, ?, ?, ?)
        """

    type PP = (ID[Game], Orientation, Int, Int)

    Update[PP](sql)
  }

  def findGameLeavesByUserId(userId: ID[User]): Query0[ID[Game]] = {
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
    """.query[ID[Game]]
  }

  def findGameBranchEndedOnGameId(
      gameId: ID[Game]
  ): Query0[ID[Game]] = {
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
    """.query[ID[Game]]
  }

  def existsGameWithId(gameId: ID[Game]): Query0[Unit] = {
    sql"""
    SELECT * FROM game_state
    WHERE id = $gameId
    """.query
  }

  def findWinnerByGameId(gameId: ID[Game]): Query0[User] = {
    sql"""
    SELECT
    "user".id,
    "user".login
    FROM game_state
    JOIN "user"
    ON "user".id = winner
    WHERE game_state.id = $gameId
    """.query[User]
  }

  def findUsersByGameId(gameId: ID[Game]): Query0[User] = {
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
    """.query[User]
  }
}
