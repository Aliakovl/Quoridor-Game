package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.api.AuthorizationServerEndpoints.*
import dev.aliakovl.quoridor.api.ErrorMapping.defaultErrorsMapping
import dev.aliakovl.quoridor.auth.AuthenticationService
import dev.aliakovl.quoridor.auth.model.RefreshToken
import dev.aliakovl.quoridor.services.UserService
import sttp.model.headers.Cookie.SameSite
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.ztapir.*
import zio.{Task, URLayer, ZIO, ZLayer}

import scala.util.chaining.*

class AuthorizationServerEndpoints(
    userService: UserService,
    authenticationService: AuthenticationService,
    authorizationAPI: AuthorizationEndpoints
):
  private val singUpServerEndpoint: ZServerEndpoint[Any, Any] =
    authorizationAPI.singUpEndpoint
      .zServerLogic { credentials =>
        userService
          .createUser(credentials)
          .zipRight(authenticationService.signIn(credentials))
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
          .pipe(defaultErrorsMapping)
      }

  private val signInServerEndpoint: ZServerEndpoint[Any, Any] =
    authorizationAPI.signInEndpoint
      .zServerLogic { credentials =>
        authenticationService
          .signIn(credentials)
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
          .pipe(defaultErrorsMapping)
      }

  private val refreshServerEndpoint: ZServerEndpoint[Any, Any] =
    authorizationAPI.refreshEndpoint
      .zServerLogic { refreshToken =>
        authenticationService
          .refresh(refreshToken)
          .flatMap { case (accessToken, refreshToken) =>
            cookieValue(refreshToken).map((accessToken, _))
          }
          .pipe(defaultErrorsMapping)
      }

  private val signOutServerEndpoint: ZServerEndpoint[Any, Any] =
    authorizationAPI.signOutEndpoint
      .zServerLogic { refreshToken =>
        authenticationService
          .signOut(refreshToken)
          .as(deletedCookie)
          .pipe(defaultErrorsMapping)
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
