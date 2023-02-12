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
    WHERE user_id = $userId
    """.query[User]
  }

  def registerUser(user: User): Update0 = {
    user match {
      case User(id, login) =>
        sql"""
        INSERT INTO "user" (user_id, login)
        VALUES ($id, $login)
        """.update
    }
  }

  def findProtoGameByGameId(gameId: ID[Game]): Query0[ProtoPlayer] = {
    sql"""
    SELECT "user".user_id, login, target
    FROM game
    JOIN player USING (game_id)
    JOIN "user" USING (user_id)
    WHERE game.game_id = $gameId
    ORDER BY player.user_id = game.creator DESC
    """.query[ProtoPlayer]
  }

  def createProtoGameByUser(
      gameId: ID[Game],
      userId: ID[User]
  ): Update0 = {
    val target = North
    sql"""
    INSERT INTO game (game_id, creator)
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

  def findWallsByGameId(gameId: ID[Game], step: Int): Query0[WallPosition] = {
    sql"""
    SELECT
    orient,
    "row",
    "column"
    FROM wall_position
    WHERE game_id = $gameId
    AND step <= $step
    """.query[WallPosition]
  }

  def findActivePlayerByGameId(gameId: ID[Game], step: Int): Query0[Player] = {
    sql"""
    SELECT
    active_player,
    login,
    "row",
    "column",
    walls_amount,
    target
    from game_state
    JOIN "user" ON active_player = "user".user_id
    JOIN pawn_position pp ON pp.game_id = game_state.game_id
    AND pp.step = game_state.step
    AND pp.user_id = active_player
    JOIN player p ON p.game_id = game_state.game_id
    AND p.user_id = active_player
    WHERE game_state.game_id = $gameId
    AND game_state.step = $step
    """.query[Player]
  }

  def findEnemiesByGameId(
      gameId: ID[Game],
      step: Int
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
    NATURAL JOIN "user" u
    NATURAL JOIN game_state gs
    JOIN player p USING (game_id, user_id)
    WHERE pp.game_id = $gameId
    AND pp.step = $step
    AND NOT pp.user_id = gs.active_player
    """.query[Player]
  }

  def recordNextState(
      gameId: ID[Game],
      step: Int,
      activePlayerId: ID[User]
  ): Update0 = {
    sql"""
    INSERT INTO game_state (game_id, step, active_player)
    VALUES ($gameId, $step, $activePlayerId);
    """.update
  }

  def recordWinner(
      gameId: ID[Game],
      userId: ID[User]
  ): Update0 = {
    sql"""
    INSERT INTO winner (game_id, user_id)
    VALUES ($gameId, $userId)
    """.update
  }

  val recordPlayers: Update[(ID[Game], Int, ID[User], Int, Int, Int)] = {
    val sql = """
        INSERT INTO pawn_position (game_id, step, user_id, walls_amount, "row", "column")
        VALUES (?, ?, ?, ?, ?, ?)
        """
    type PP = (ID[Game], Int, ID[User], Int, Int, Int)

    Update[PP](sql)
  }

  def recordWall(
      gameId: ID[Game],
      step: Int,
      orientation: Orientation,
      row: Int,
      column: Int
  ): Update0 = {
    sql"""
    INSERT INTO wall_position (game_id, step, orient, "row", "column")
    VALUES ($gameId, $step, $orientation, $row, $column)
    """.update
  }

  def findUserGames(userId: ID[User]): Query0[ID[Game]] = {
    sql"""
    SELECT
    game_id
    FROM player
    WHERE user_id = $userId
    """.query[ID[Game]]
  }

  def lastStep(
      gameId: ID[Game]
  ): Query0[Int] = {
    sql"""
    SELECT
    MAX(step)
    FROM game_state
    WHERE game_id = $gameId
    """.query[Int]
  }

  def hasStarted(gameId: ID[Game]): Query0[Unit] = {
    sql"""
    SELECT * FROM game_state
    WHERE game_id = $gameId
    AND step = 0
    """.query
  }

  def findWinnerByGameId(gameId: ID[Game]): Query0[User] = {
    sql"""
    SELECT
    "user".user_id,
    "user".login
    FROM winner
    NATURAL JOIN "user"
    WHERE game_id = $gameId
    """.query[User]
  }

  def findUsersByGameId(gameId: ID[Game]): Query0[User] = {
    sql"""
    SELECT
    "user".user_id,
    "user".login
    FROM player
    NATURAL JOIN "user"
    WHERE game_id = $gameId
    """.query[User]
  }
}
