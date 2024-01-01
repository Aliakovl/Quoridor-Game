package ru.quoridor.api

import ru.quoridor.api.AuthorizationServerEndpoints.*
import ru.quoridor.auth.AuthenticationService
import ru.quoridor.auth.model.RefreshToken
import ru.quoridor.services.UserService
import sttp.model.headers.Cookie.SameSite
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.ztapir.*
import zio.{Task, URLayer, ZIO, ZLayer}

class AuthorizationServerEndpoints(
    userService: UserService,
    authenticationService: AuthenticationService,
    authorizationAPI: AuthorizationEndpoints
):
  val singUpServerEndpoint: ZServerEndpoint[Any, Any] =
    authorizationAPI.singUpEndpoint
      .zServerLogic { credentials =>
        userService
          .createUser(credentials)
          .zipRight(authenticationService.signIn(credentials))
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
      }

  val signInServerEndpoint: ZServerEndpoint[Any, Any] =
    authorizationAPI.signInEndpoint
      .zServerLogic { credentials =>
        authenticationService
          .signIn(credentials)
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
      }

  val refreshServerEndpoint: ZServerEndpoint[Any, Any] =
    authorizationAPI.refreshEndpoint
      .zServerLogic { refreshToken =>
        authenticationService
          .refresh(refreshToken)
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
      }

  val signOutServerEndpoint: ZServerEndpoint[Any, Any] =
    authorizationAPI.signOutEndpoint
      .zServerLogic { refreshToken =>
        authenticationService.signOut(refreshToken).as(deletedCookie)
      }

  val endpoints: List[ZServerEndpoint[Any, Any]] = List(
    singUpServerEndpoint,
    signInServerEndpoint,
    refreshServerEndpoint,
    signOutServerEndpoint
  )

object AuthorizationServerEndpoints:
  val live: URLayer[
    UserService & AuthenticationService & AuthorizationEndpoints,
    AuthorizationServerEndpoints
  ] = ZLayer.fromFunction(new AuthorizationServerEndpoints(_, _, _))

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

  private val deletedCookie =
    CookieValueWithMeta.unsafeApply("", maxAge = Some(0))
