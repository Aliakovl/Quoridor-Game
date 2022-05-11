package model.api

import cats.effect.IO
import model.{ExceptionResponse, User}
import model.services.UserService
import sttp.tapir.json.circe.jsonBody
import sttp.tapir._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._


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

  val api = List(createUser)
}