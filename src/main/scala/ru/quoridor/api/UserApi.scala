package ru.quoridor.api

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import org.reactormonk.CryptoBits
import ru.quoridor.{ExceptionResponse, User}
import ru.quoridor.services.UserService
import sttp.model.StatusCode
import sttp.model.headers.CookieValueWithMeta
import zio.ZIO

case class Login(login: String)

class UserApi(userService: UserService, crypto: CryptoBits) extends TapirApi {

  private val en = endpoint.in("api")

  private val createUser: ZServerEndpoint[Any, Any] = en.post
    .in("register")
    .in(jsonBody[Login])
    .errorOut(jsonBody[ExceptionResponse])
    .out(oneOf(oneOfVariant(StatusCode.Created, jsonBody[User])))
    .out(setCookie("auth-cookie"))
    .zServerLogic { case Login(login) =>
      val response = for {
        user <- userService.createUser(login)
        clock = java.time.Clock.systemUTC
        cookie <- ZIO.succeed(
          CookieValueWithMeta.unsafeApply(
            crypto.signToken(user.id.toString, clock.millis.toString),
            secure = false,
            path = Some("/")
          )
        )
      } yield (user, cookie)

      response.mapError(ExceptionResponse.apply)
    }

  private val loginUser: ZServerEndpoint[Any, Any] = en.post
    .in("login")
    .in(jsonBody[Login])
    .errorOut(jsonBody[ExceptionResponse])
    .out(oneOf(oneOfVariant(StatusCode.Created, jsonBody[User])))
    .out(setCookie("auth-cookie"))
    .zServerLogic { case Login(login) =>
      val response = for {
        user <- userService.findUser(login)
        clock = java.time.Clock.systemUTC
        cookie <- ZIO.succeed(
          CookieValueWithMeta.unsafeApply(
            crypto.signToken(user.id.toString, clock.millis.toString),
            secure = false,
            path = Some("/")
          )
        )
      } yield (user, cookie)

      response.mapError(ExceptionResponse.apply)
    }

  private val getUser: ZServerEndpoint[Any, Any] = en.get
    .in("user" / path[String]("Login"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[User])
    .zServerLogic { login =>
      userService
        .findUser(login)
        .mapError(ExceptionResponse.apply)
    }

  override val api = List(createUser, loginUser, getUser)
}
