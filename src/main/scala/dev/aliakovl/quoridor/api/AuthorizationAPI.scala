package dev.aliakovl.quoridor.api

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.*
import sttp.tapir.generic.auto.*
import io.circe.generic.auto.*
import dev.aliakovl.quoridor.auth.AuthenticationService
import dev.aliakovl.quoridor.auth.AuthenticationService.*
import dev.aliakovl.quoridor.auth.model.{AccessToken, Credentials, RefreshToken}
import dev.aliakovl.quoridor.services.UserService
import dev.aliakovl.quoridor.services.UserService.createUser
import sttp.model.StatusCode
import sttp.model.headers.Cookie.SameSite
import sttp.model.headers.CookieValueWithMeta
import zio.{Task, ZIO}

object AuthorizationAPI:
  def apply[Env <: UserService with AuthenticationService]
      : List[ZServerEndpoint[Env, Any]] = List(
    singUpEndpoint.widen[Env],
    signInEndpoint.widen[Env],
    refreshEndpoint.widen[Env],
    signOutEndpoint.widen[Env]
  )

  private val baseEndpoint =
    endpoint
      .in("auth")
      .errorOut(jsonBody[ExceptionResponse] and statusCode)
      .mapErrorOut(er => new Throwable(er._1.errorMessage))(
        ExceptionResponse(_)
      )

  private val singUpEndpoint =
    baseEndpoint.post
      .in("sign-up")
      .in(jsonBody[Credentials])
      .out(plainBody[AccessToken])
      .out(setCookie("refreshToken"))
      .out(statusCode(StatusCode.Created))
      .zServerLogic { credentials =>
        createUser(credentials)
          .zipRight(signIn(credentials))
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
      }

  private val signInEndpoint =
    baseEndpoint.post
      .in("sign-in")
      .in(jsonBody[Credentials])
      .out(plainBody[AccessToken])
      .out(setCookie("refreshToken"))
      .zServerLogic { credentials =>
        signIn(credentials)
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
      }

  private val refreshEndpoint =
    baseEndpoint.post
      .in("refresh")
      .in(cookie[RefreshToken]("refreshToken"))
      .out(plainBody[AccessToken])
      .out(setCookie("refreshToken"))
      .zServerLogic { refreshToken =>
        refresh(refreshToken)
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
      }

  private val signOutEndpoint =
    baseEndpoint.post
      .in("sign-out")
      .in(cookie[RefreshToken]("refreshToken"))
      .out(setCookie("refreshToken"))
      .zServerLogic { refreshToken =>
        signOut(refreshToken).as(deleteCookie)
      }

  private def cookieValue(
      refreshToken: RefreshToken
  ): Task[CookieValueWithMeta] = ZIO.attempt(
    CookieValueWithMeta.unsafeApply(
      refreshToken.value,
      maxAge = Some(2592000),
      path = Some("/auth"),
      secure = true,
      httpOnly = true,
      sameSite = Some(SameSite.Strict)
    )
  )

  private val deleteCookie =
    CookieValueWithMeta.unsafeApply("", maxAge = Some(0))
