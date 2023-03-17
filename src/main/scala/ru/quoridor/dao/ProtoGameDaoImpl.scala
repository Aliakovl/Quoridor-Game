package ru.quoridor.dao

import io.getquill.Ord
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.model.GameException.GameNotFoundException
import ru.quoridor.model.game.Game
import ru.quoridor.model.game.geometry.Side
import ru.quoridor.model.{ProtoGame, ProtoPlayer, ProtoPlayers, User}
import ru.utils.tagging.ID
import zio.{Task, ZIO}

class ProtoGameDaoImpl(quillContext: QuillContext) extends ProtoGameDao {
  import quillContext._

  override def find(gameId: ID[Game]): Task[ProtoGame] = {
    val findProtoPlayersByGameId = quote {
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

    run(findProtoPlayersByGameId).flatMap {
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
    val insertNewGame = quote {
      query[dto.Game].insert(
        _.gameId -> lift(gameId),
        _.creator -> lift(userId)
      )
    }

    transaction {
      run(insertNewGame) *>
        addPlayer(gameId, userId, target)
    }
  }

  override def addPlayer(
      gameId: ID[Game],
      userId: ID[User],
      target: Side
  ): Task[Unit] = {
    val insertPlayer = quote {
      query[dto.Player].insert(
        _.gameId -> lift(gameId),
        _.userId -> lift(userId),
        _.target -> lift(target)
      )
    }

    run(insertPlayer).unit
  }
}
