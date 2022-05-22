package ru.quoridor.api

import cats.effect.IO
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import io.circe.generic.auto.{exportDecoder, exportEncoder}
import io.circe.{Encoder, Json, parser}
import io.circe.syntax.EncoderOps
import ru.quoridor.ExceptionResponse.ExceptionResponse400
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonEncoder
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, Response, StaticFile}
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import ru.quoridor.{ExceptionResponse, GameException, GameMoveException, User}
import ru.quoridor.game.{Game, Move}
import ru.quoridor.services.GameService
import ru.utils.Typed.ID
import ru.utils.Typed.Implicits.TypedOps

import java.util.UUID
import scala.collection.concurrent.TrieMap


case class UserMove(id: ID[User], move: Move)

class WSGameApi(wsb: WebSocketBuilder2[IO],
                gameService: GameService[IO]) {

  def logic(sessionId: UUID, topic: Topic[IO, WebSocketFrame]): IO[Response[IO]] = {
    def toClient: Stream[IO, WebSocketFrame] =
      topic.subscribe(1000)

    def fromClient: Pipe[IO, WebSocketFrame, Unit] =
      handle(sessionId, topic)

    wsb.build(toClient, fromClient)
  }

  implicit val jsonEncode: Encoder[ExceptionResponse] = Encoder.forProduct1("errorMessage")(_.message)

  private def handle(sessionId: UUID, topic: Topic[IO, WebSocketFrame]): Pipe[IO, WebSocketFrame, Unit] = in =>
    in.collect({
      case WebSocketFrame.Text(text, _) =>
        parser.parse(text).flatMap(_.as[UserMove])
    })
    .evalMap {
      case Right(UserMove(userId, move)) =>
        val ws = for {
          gameId <- IO.fromOption(SessionsMap.gameStates.get(sessionId))(new Exception("There is no such session"))
          game <- gameService.makeMove(gameId.typed[Game], userId, move)
          _ = SessionsMap.gameStates.update(sessionId, game.id.unType)
        } yield WebSocketFrame.Text(game.asJson.toString())

        ws.handleError{ er => WebSocketFrame.Text(ExceptionResponse(er).asJson.toString()) }

      case Left(_) => IO.pure(WebSocketFrame.Text(ExceptionResponse400("Not able to parse input data").asJson.toString()))

    }.through(topic.publish)


  def routeWs: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "session" / UUIDVar(sessionId) if SessionsMap.gameStates.contains(sessionId) =>
      SessionsMap.sessions.get(sessionId) match {
        case Some(topic) =>
          logic(sessionId, topic)
        case None => for {
          topic <- Topic[IO, WebSocketFrame]
          _ = SessionsMap.sessions.update(sessionId, topic)
          l <- logic(sessionId, topic)
        } yield l
      }

    case POST -> Root / "create" / UUIDVar(gameId) =>
      val sessionId = UUID.randomUUID()
      SessionsMap.gameStates.update(sessionId, gameId)
      IO(Response(Created).withEntity(parser.parse(s"""{"sessionId": "$sessionId"}""").getOrElse(Json.Null)))

    case GET -> Root / "game" / UUIDVar(sessionId) / UUIDVar(userId) if SessionsMap.gameStates.contains(sessionId) =>
      val gameId = SessionsMap.gameStates.get(sessionId)
      for {
        game <- gameService.findGame(gameId.get.typed[Game], userId.typed[User])
      } yield Response(Ok).withEntity(game)
  }
}

object SessionsMap {
  val sessions: TrieMap[UUID, Topic[IO, WebSocketFrame]] = TrieMap[UUID, Topic[IO, WebSocketFrame]]().empty

  val gameStates: TrieMap[UUID, UUID] = TrieMap[UUID, UUID]().empty
}