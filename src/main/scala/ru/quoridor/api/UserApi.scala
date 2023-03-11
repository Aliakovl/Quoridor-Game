package ru.quoridor.api

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ru.quoridor.auth.model.{Credentials, Username}
import ru.quoridor.model.User
import ru.quoridor.services.UserService
import ru.quoridor.services.UserService.findUser
import sttp.model.StatusCode

object UserApi {

  private val baseEndpoint =
    endpoint.in("api").errorOut(jsonBody[ExceptionResponse])

  val createUser: ZServerEndpoint[UserService, Any] = baseEndpoint.post
    .in("register")
    .in(jsonBody[Credentials])
    .out(statusCode(StatusCode.Created).and(jsonBody[User]))
    .zServerLogic { credentials =>
      UserService.createUser(credentials).mapError(ExceptionResponse.apply)
    }

  val loginUser: ZServerEndpoint[UserService, Any] = baseEndpoint.post
    .in("login")
    .in(jsonBody[Login])
    .out(jsonBody[User])
    .zServerLogic { case Login(login) =>
      findUser(login).mapError(ExceptionResponse.apply)
    }

  val getUser: ZServerEndpoint[UserService, Any] = baseEndpoint.get
    .in("user" / path[Username]("Login"))
    .out(jsonBody[User])
    .zServerLogic { username =>
      findUser(username).mapError(ExceptionResponse.apply)
    }

}
