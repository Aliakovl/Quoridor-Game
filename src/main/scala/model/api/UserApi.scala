package model.api

import cats.effect.IO
import cats.implicits._
import model._
import model.ExceptionResponse._
import model.services.UserService
import sttp.tapir.json.circe.jsonBody
import sttp.tapir._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import org.reactormonk.CryptoBits
import sttp.model.StatusCode
import sttp.model.headers.CookieValueWithMeta



case class Login(login: String)

class UserApi(userService: UserService[IO],
              crypto: CryptoBits) extends TapirApi {

  private val createUser = endpoint.post
    .in("register")
    .in(jsonBody[Login])
    .errorOut(oneOf(
      oneOfVariant(StatusCode.BadRequest, jsonBody[ExceptionResponse400]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(oneOf(oneOfVariant(StatusCode.Created, jsonBody[User])))
    .out(setCookie("auth-cookie"))
    .serverLogic[IO] { case Login(login) =>
      val response = for {
        user <- userService.createUser(login)
        clock = java.time.Clock.systemUTC
        cookie <- IO(CookieValueWithMeta.unsafeApply(crypto.signToken(user.id.toString, clock.millis.toString), secure = false, path = Some("/")))
      } yield Right(user, cookie)

      response.handleError { er => ExceptionResponse(er).asLeft }
    }

  private val loginUser = endpoint.post
    .in("login")
    .in(jsonBody[Login])
    .errorOut(oneOf(
      oneOfVariant(StatusCode.NotFound, jsonBody[ExceptionResponse404]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(oneOf(oneOfVariant(StatusCode.Created, jsonBody[User])))
    .out(setCookie("auth-cookie"))
    .serverLogic[IO] { case Login(login) =>
      val response = for {
        user <- userService.findUser(login)
        clock = java.time.Clock.systemUTC
        cookie <- IO(CookieValueWithMeta.unsafeApply(crypto.signToken(user.id.toString, clock.millis.toString), secure = false, path = Some("/")))
      } yield Right(user, cookie)

      response.handleError{ er => ExceptionResponse(er).asLeft }
    }

  private val getUser = endpoint.get
    .in("user" / path[String]("Login"))
    .errorOut(oneOf(
      oneOfVariant(StatusCode.NotFound, jsonBody[ExceptionResponse404]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(jsonBody[User])
    .serverLogic[IO] { login =>
      userService.findUser(login).map(Right(_)).handleError{ er =>
        ExceptionResponse(er).asLeft
      }
    }

  override val api = List(createUser, loginUser, getUser)
}