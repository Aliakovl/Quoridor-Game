package ru.quoridor.api

import cats.implicits._
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import io.circe.generic.auto.{exportDecoder, exportEncoder}
import io.circe.{Encoder, Json, parser}
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonEncoder
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, Response}
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import ru.quoridor.app.QuoridorGame.Env
import ru.quoridor.{ExceptionResponse, User}
import ru.quoridor.game.{Game, Move}
import ru.quoridor.services.GameService
import ru.utils.Typed.ID
import ru.utils.Typed.Implicits.TypedOps
import zio.interop.catz._
import zio.{RIO, ZIO}

import java.util.UUID
import scala.collection.concurrent.TrieMap

case class UserMove(id: ID[User], move: Move)

class WSGameApi[R <: Env](
    wsb: WebSocketBuilder2[RIO[R, _]]
) {

  private def logic(
      sessionId: UUID,
      topic: Topic[RIO[R, _], WebSocketFrame]
  ): RIO[R, Response[RIO[R, _]]] = {
    def toClient: Stream[RIO[R, _], WebSocketFrame] =
      topic.subscribe(1000)

    def fromClient: Pipe[RIO[R, _], WebSocketFrame, Unit] =
      handle(sessionId, topic)

    wsb.build(toClient, fromClient)
  }

  implicit val jsonEncode: Encoder[ExceptionResponse] =
    Encoder.forProduct1("errorMessage")(_.errorMessage)

  private def handle(
      sessionId: UUID,
      topic: Topic[RIO[R, _], WebSocketFrame]
  ): Pipe[RIO[R, _], WebSocketFrame, Unit] = in =>
    in.collect({ case WebSocketFrame.Text(text, _) =>
      parser.parse(text).flatMap(_.as[UserMove])
    }).evalMap {
      case Right(UserMove(userId, move)) =>
        val ws = for {
          gameId <- ZIO
            .fromOption(SessionsMap.gameStates.get(sessionId))
            .orElseFail(new Exception("There is no such session"))
          gameService <- ZIO.service[GameService]
          game <- gameService.makeMove(gameId.typed[Game], userId, move)
          _ = SessionsMap.gameStates.update(sessionId, game.id.unType)
        } yield WebSocketFrame.Text(game.asJson.toString())

        ws.handleError { er =>
          WebSocketFrame.Text(ExceptionResponse(er).asJson.toString())
        }

      case Left(_) =>
        ZIO.succeed(
          WebSocketFrame.Text(
            ExceptionResponse("Not able to parse input data").asJson.toString()
          )
        )

    }.through(topic.publish)

  def routeWs: HttpRoutes[RIO[R, _]] = HttpRoutes.of[RIO[R, _]] {
    case GET -> Root / "session" / UUIDVar(sessionId)
        if SessionsMap.gameStates.contains(sessionId) =>
      SessionsMap.sessions.get(sessionId) match {
        case Some(topic) =>
          logic(sessionId, topic)
        case None =>
          for {
            topic <- Topic[RIO[R, _], WebSocketFrame]
            _ = SessionsMap.sessions.update(sessionId, topic)
            l <- logic(sessionId, topic)
          } yield l
      }

    case POST -> Root / "create" / UUIDVar(gameId) =>
      val sessionId = UUID.randomUUID()
      SessionsMap.gameStates.update(sessionId, gameId)
      for {
        gameService <- ZIO.service[GameService]
        game <- gameService.findGame(gameId.typed[Game])
        players = game.state.players.toList.map(_.id.unType)
        _ = SessionsMap.sessionPlayers.update(sessionId, players)
      } yield Response(Created).withEntity(
        parser.parse(s"""{"sessionId": "$sessionId"}""").getOrElse(Json.Null)
      )

    case GET -> Root / "game" / UUIDVar(sessionId) / UUIDVar(userId)
        if SessionsMap.gameStates.contains(sessionId) =>
      val gameId = SessionsMap.gameStates.get(sessionId)
      for {
        gameService <- ZIO.service[GameService]
        game <- gameService.findGame(gameId.get.typed[Game])
      } yield Response(Ok).withEntity(game)

    case GET -> Root / "current-sessions" / UUIDVar(userId) =>
      val currentSessions: List[UUID] = SessionsMap.sessionPlayers
        .filter { case (_, players) =>
          players.contains(userId)
        }
        .keys
        .toList
      ZIO.succeed(Response(Ok).withEntity(currentSessions))
  }

  object SessionsMap {
    val sessions: TrieMap[UUID, Topic[RIO[R, _], WebSocketFrame]] =
      TrieMap[UUID, Topic[RIO[R, _], WebSocketFrame]]().empty

    val gameStates: TrieMap[UUID, UUID] = TrieMap[UUID, UUID]().empty

    val sessionPlayers: TrieMap[UUID, List[UUID]] =
      TrieMap[UUID, List[UUID]]().empty
  }
}
