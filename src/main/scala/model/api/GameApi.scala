package model.api

import cats.effect.IO
import model.services.{GameCreator, GameService, UserService}
import model.ExceptionResponse
import model.ProtoGame
import model.User
import utils.Typed.Implicits._
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto._
import io.circe.generic.auto._

import java.util.UUID

class GameApi(userService: UserService[IO],
              gameCreator: GameCreator[IO],
              gameService: GameService[IO]) extends TapirApi {

  private val ep = endpoint.in("api")

  private val createGame = ep.post
    .in(path[UUID]("userId"))
    .in("create-game")
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[ProtoGame])
    .serverLogic[IO] { uuid =>
      gameCreator.createGame(uuid.typed[User]).map(x => Right(x)).handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }
  
  val api = List(createGame)
}
