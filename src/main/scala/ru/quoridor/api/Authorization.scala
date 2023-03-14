package ru.quoridor.api

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ru.quoridor.auth.AuthenticationService
import ru.quoridor.auth.AuthenticationService._
import ru.quoridor.auth.model.{AccessToken, Credentials, RefreshToken}
import ru.quoridor.services.UserService
import ru.quoridor.services.UserService.createUser
import sttp.model.StatusCode
import sttp.model.headers.Cookie.SameSite
import sttp.model.headers.CookieValueWithMeta
import zio.{Task, ZIO}

object Authorization {

  private val baseEndpoint =
    endpoint
      .in("auth")
      .errorOut(jsonBody[ExceptionResponse])

  val singOnEndpoint
      : ZServerEndpoint[UserService with AuthenticationService, Any] =
    baseEndpoint.post
      .in("sign-on")
      .in(jsonBody[Credentials])
      .out(jsonBody[AccessToken])
      .out(setCookie("refreshToken"))
      .out(statusCode(StatusCode.Created))
      .zServerLogic { credentials =>
        createUser(credentials)
          .zipRight(signIn(credentials))
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
          .mapError(ExceptionResponse.apply)
      }

  val signInEndpoint: ZServerEndpoint[AuthenticationService, Any] =
    baseEndpoint.put
      .in("sign-in")
      .in(jsonBody[Credentials])
      .out(jsonBody[AccessToken])
      .out(setCookie("refreshToken"))
      .zServerLogic { credentials =>
        signIn(credentials)
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
          .mapError(ExceptionResponse.apply)
      }

  val refreshEndpoint: ZServerEndpoint[AuthenticationService, Any] =
    baseEndpoint.get
      .in("refresh")
      .in(cookie[RefreshToken]("refreshToken"))
      .securityIn(auth.bearer[AccessToken]())
      .out(jsonBody[AccessToken])
      .out(setCookie("refreshToken"))
      .zServerSecurityLogic { accessToken =>
        ZIO.succeed(accessToken): ZIO[
          AuthenticationService,
          Nothing,
          AccessToken
        ]
      }
      .serverLogic { accessToken => refreshToken =>
        refresh(accessToken, refreshToken)
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
          .mapError(ExceptionResponse.apply)
      }

  val signOutEndpoint: ZServerEndpoint[AuthenticationService, Any] =
    baseEndpoint.post
      .in("sign-out")
      .in(cookie[RefreshToken]("refreshToken"))
      .securityIn(auth.bearer[AccessToken]())
      .out(setCookie("refreshToken"))
      .zServerSecurityLogic { accessToken =>
        ZIO.succeed(accessToken): ZIO[
          AuthenticationService,
          Nothing,
          AccessToken
        ]
      }
      .serverLogic { accessToken => refreshToken =>
        signOut(accessToken, refreshToken)
          .zipRight(ZIO.attempt(CookieValueWithMeta.unsafeApply("")))
          .mapError(ExceptionResponse.apply)
      }

  private def cookieValue(
      refreshToken: RefreshToken
  ): Task[CookieValueWithMeta] = ZIO.attempt(
    CookieValueWithMeta.unsafeApply(
      refreshToken.value.toString,
      maxAge = Some(2592000),
      path = Some("/auth"),
      secure = true,
      httpOnly = true,
      sameSite = Some(SameSite.Strict)
    )
  )
}
