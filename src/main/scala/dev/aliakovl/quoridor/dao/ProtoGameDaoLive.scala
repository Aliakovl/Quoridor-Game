package dev.aliakovl.quoridor.dao

import dev.aliakovl.quoridor.dao.quill.QuillContext
import dev.aliakovl.quoridor.GameException.{
  GameNotFoundException,
  SamePlayerException
}
import dev.aliakovl.quoridor.engine.game.geometry.Side
import dev.aliakovl.quoridor.model.{
  Game,
  ProtoGame,
  ProtoPlayer,
  ProtoPlayers,
  User
}
import dev.aliakovl.utils.tagging.ID
import io.getquill.*
import org.postgresql.util.PSQLState
import zio.{RLayer, Task, ZIO, ZLayer}

import java.sql.SQLException

class ProtoGameDaoLive(quillContext: QuillContext) extends ProtoGameDao:
  import quillContext.*
  import quillContext.given

  override def find(gameId: ID[Game]): Task[ProtoGame] = {
    val findProtoPlayersByGameId = run {
      {
        for {
          player <- query[dto.Player]
          user <- query[dto.Userdata].join(_.userId == player.userId)
          game <- query[dto.Game].join(_.gameId == player.gameId)
          if game.gameId == lift(gameId)
        } yield (
          ProtoPlayer(user.userId, user.username, player.target),
          game.creator
        )
      }.sortBy { case (protoPlayer, creator) => protoPlayer.id == creator }(
        Ord.desc
      ).map(_._1)
    }

    findProtoPlayersByGameId.flatMap {
      case Nil => ZIO.fail(GameNotFoundException(gameId))
      case creator :: guests =>
        ZIO.succeed(ProtoGame(gameId, ProtoPlayers(creator, guests)))
    }
  }

  override def insert(
      gameId: ID[Game],
      userId: ID[User],
      target: Side
  ): Task[Unit] = {
    inline def insertNewGame() = quote {
      query[dto.Game].insert(
        _.gameId -> lift(gameId),
        _.creator -> lift(userId)
      )
    }

    transaction {
      run(insertNewGame()) *>
        addPlayer(gameId, userId, target)
    }
  }

  override def addPlayer(
      gameId: ID[Game],
      userId: ID[User],
      target: Side
  ): Task[Unit] = {
    inline def insertPlayer() = quote {
      query[dto.Player].insert(
        _.gameId -> lift(gameId),
        _.userId -> lift(userId),
        _.target -> lift(target)
      )
    }

    run(insertPlayer()).unit
      .catchSome {
        case x: SQLException
            if x.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
          ZIO.fail(SamePlayerException(userId, gameId))
      }
  }

object ProtoGameDaoLive:
  val live: RLayer[QuillContext, ProtoGameDao] =
    ZLayer.fromFunction(new ProtoGameDaoLive(_))
