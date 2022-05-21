package model.api

import cats.effect.IO
import model.{ExceptionResponse, User}
import model.services.UserService
import sttp.tapir.json.circe.jsonBody
import sttp.tapir._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import org.reactormonk.CryptoBits
import sttp.model.headers.CookieValueWithMeta



case class Login(login: String)

class UserApi(userService: UserService[IO],
              crypto: CryptoBits) extends TapirApi {

  private val createUser = endpoint.post
    .in("register")
    .in(jsonBody[Login])
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[User])
    .out(setCookie("auth-cookie"))
    .serverLogic[IO] { case Login(login) =>
      val response = for {
        user <- userService.createUser(login)
        clock = java.time.Clock.systemUTC
        cookie <- IO(CookieValueWithMeta.unsafeApply(crypto.signToken(user.id.toString, clock.millis.toString), secure = false, path = Some("/")))
      } yield Right(user, cookie)

      response.handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val loginUser = endpoint.post
    .in("login")
    .in(jsonBody[Login])
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[User])
    .out(setCookie("auth-cookie"))
    .serverLogic[IO] { case Login(login) =>
      val response = for {
        user <- userService.findUser(login)
        clock = java.time.Clock.systemUTC
        cookie <- IO(CookieValueWithMeta.unsafeApply(crypto.signToken(user.id.toString, clock.millis.toString), secure = false, path = Some("/")))
      } yield Right(user, cookie)

      response.handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val getUser = endpoint.get
    .in("user" / path[String]("Login"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[User])
    .serverLogic[IO] { login =>
      userService.findUser(login).map(Right(_)).handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  override val api = List(createUser, loginUser, getUser)
}