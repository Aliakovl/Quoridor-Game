package model.api

import cats.effect.IO
import model.{ExceptionResponse, User}
import model.services.UserService
import model.GamePreView
import sttp.tapir.json.circe.jsonBody
import sttp.tapir._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import utils.Typed.Implicits.TypedOps

import java.util.UUID


case class Login(login: String)

class UserApi(userService: UserService[IO]) extends TapirApi {

  private val ep = endpoint.in("api")

  private val createUser = ep.post
    .in("register")
    .in(jsonBody[Login])
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[User])
    .serverLogic[IO] { case Login(login) =>
      userService.createUser(login).map(Right(_)).handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val historyEndpoint = ep.get
    .in(path[UUID]("userId"))
    .in("history")
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[List[GamePreView]])
    .serverLogic[IO] { userId =>
      userService.usersHistory(userId.typed[User]).map(Right(_)).handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  val api = List(createUser, historyEndpoint)
}