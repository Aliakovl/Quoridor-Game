package ru.quoridor.storage.quillInst

import io.getquill._
import io.getquill.jdbczio.Quill
import org.postgresql.util.PGobject
import ru.quoridor.model.game.geometry.Side.toEnum
import ru.quoridor.model.game.geometry.{Orientation, PawnPosition, Side}
import ru.quoridor.model.game.{Game, Player}
import ru.quoridor.model.{ProtoPlayer, User}
import ru.quoridor.storage.dto
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.Implicits.TaggedOps

import java.sql.Types
import java.util.UUID

class DataService(quill: Quill.Postgres[SnakeCase]) {
  import quill._

//  implicit def taggedRncoder[A, B]: MappedEncoding[A, A @@ B] =
//    MappedEncoding[A, A @@ B](_.tag[B])
//
//  implicit def taggedDecoder[A, B]: MappedEncoding[A @@ B, A] =
//    MappedEncoding[A @@ B, A](_.untag)

  implicit val sideEncoder: Encoder[Side] =
    encoder[Side](
      Types.OTHER,
      (index: Index, value: Side, row: PrepareRow) => {
        val pgObj = new PGobject()
        pgObj.setType("side")
        pgObj.setValue(toEnum(value))
        row.setObject(index, pgObj, Types.OTHER)
      }
    )

  implicit val sideDecoder: Decoder[Side] =
    decoder[Side]((row: ResultRow) =>
      (index: Index) => Side.withName(row.getObject(index).toString)
    )

  implicit val orientationDecoder: Decoder[Orientation] =
    decoder[Orientation]((row: ResultRow) =>
      (index: Index) => Orientation.withName(row.getObject(index).toString)
    )

  def findUserByLogin(login: String): Quoted[Query[dto.User]] = quote {
    query[dto.User].filter(user => user.login == lift(login))
  }

  def findUserById(userId: ID[User]): Quoted[Query[dto.User]] = quote {
    query[dto.User].filter(user => user.id == lift(userId))
  }

  def registerUser(login: String): Quoted[Insert[dto.User]] = { // DTO
    val userId = UUID.randomUUID().tag[User]
    quote {
      query[dto.User].insertValue(lift(dto.User(userId, login)))
    }
  }

  def findProtoGameByGameId(gameId: ID[Game]): Quoted[Query[ProtoPlayer]] = {
    quote {
      for {
        game <- query[dto.Game]
        player <- query[dto.Player].join(_.gameId == game.id)
        user <- query[dto.User].join(_.id == player.userId)
        if game.id == lift(gameId)
      } yield ProtoPlayer(user.id, user.login, player.target)
    }
  }

  def createProtoGameByUser(
      gameId: ID[Game],
      userId: ID[User]
  ): Quoted[Insert[dto.Player]] = { // разделить на
//    quote {
//      query[dto.Game].insert(
//        _.id -> lift(gameId),
//        _.creator -> lift(userId)
//      )
//    }

    quote {
      query[dto.Player].insert(
        _.gameId -> lift(gameId),
        _.userId -> lift(userId),
        _.target -> lift(Side.North)
      )
    }
  }

  def addUserIntoProtoGame(
      gameId: ID[Game],
      userId: ID[User],
      target: Side
  ): Quoted[Insert[dto.Player]] = quote {
    query[dto.Player].insert(
      _.gameId -> lift(gameId),
      _.userId -> lift(userId),
      _.target -> target
    )
  }

  def findWallsByGameId(
      gameId: ID[Game]
  ): Quoted[Query[dto.WallPosition]] = quote {
    query[dto.WallPosition].filter(_.gameStateId == gameId)
  }

  def findActivePlayerByGameId(gameId: ID[Game]): Quoted[Query[Player]] =
    quote {
      for {
        gameState <- query[dto.GameState]
        user <- query[dto.User].join(_.id == gameState.activePlayer)
        pawnPosition <- query[dto.PawnPosition].join(pp =>
          pp.gameStateId == gameState.id && pp.userId == gameState.activePlayer
        )
        player <- query[dto.Player].join(p =>
          p.gameId == gameState.gameId && p.userId == gameState.activePlayer
        )
        if gameState.id == gameId
      } yield Player(
        user.id,
        user.login,
        PawnPosition(pawnPosition.row, pawnPosition.column),
        pawnPosition.wallsAmount,
        player.target
      )
    }

}
