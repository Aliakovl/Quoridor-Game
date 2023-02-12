package ru.quoridor.api

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ru.quoridor.model.User
import ru.quoridor.services.UserService
import ru.quoridor.services.UserService.findUser
import sttp.model.StatusCode

object UserApi {

  private val en = endpoint.in("api")

  val createUser: ZServerEndpoint[UserService, Any] = en.post
    .in("register")
    .in(jsonBody[Login])
    .errorOut(jsonBody[ExceptionResponse])
    .out(statusCode(StatusCode.Created).and(jsonBody[User]))
    .zServerLogic { case Login(login) =>
      UserService.createUser(login).mapError(ExceptionResponse.apply)
    }

  val loginUser: ZServerEndpoint[UserService, Any] = en.post
    .in("login")
    .in(jsonBody[Login])
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[User])
    .zServerLogic { case Login(login) =>
      findUser(login).mapError(ExceptionResponse.apply)
    }

  val getUser: ZServerEndpoint[UserService, Any] = en.get
    .in("user" / path[String]("Login"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[User])
    .zServerLogic { login =>
      findUser(login).mapError(ExceptionResponse.apply)
    }

}
