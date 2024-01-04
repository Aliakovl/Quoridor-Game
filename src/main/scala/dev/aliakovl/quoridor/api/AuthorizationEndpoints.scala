package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.auth.model.{AccessToken, Credentials, RefreshToken}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.*
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.Endpoint
import zio.{URLayer, ZLayer}

class AuthorizationEndpoints(base: BaseEndpoints):
  val singUpEndpoint: Endpoint[
    Unit,
    Credentials,
    Throwable,
    (AccessToken, CookieValueWithMeta),
    Any
  ] =
    base.publicEndpoint.post
      .tag("Authentication")
      .name("sign-up")
      .summary("Sign up user")
      .in("auth" / "sign-up")
      .in(jsonBody[Credentials])
      .out(plainBody[AccessToken])
      .out(setCookie("refreshToken"))
      .out(statusCode(StatusCode.Created))

  val signInEndpoint: Endpoint[
    Unit,
    Credentials,
    Throwable,
    (AccessToken, CookieValueWithMeta),
    Any
  ] =
    base.publicEndpoint.post
      .tag("Authentication")
      .name("sign-in")
      .summary("Sign in user")
      .in("auth" / "sign-in")
      .in(jsonBody[Credentials])
      .out(plainBody[AccessToken])
      .out(setCookie("refreshToken"))

  val refreshEndpoint: Endpoint[
    Unit,
    RefreshToken,
    Throwable,
    (AccessToken, CookieValueWithMeta),
    Any
  ] =
    base.publicEndpoint.post
      .tag("Authentication")
      .name("refresh")
      .summary("Refresh user`s access token")
      .in("auth" / "refresh")
      .in(cookie[RefreshToken]("refreshToken"))
      .out(plainBody[AccessToken])
      .out(setCookie("refreshToken"))

  val signOutEndpoint
      : Endpoint[Unit, RefreshToken, Throwable, CookieValueWithMeta, Any] =
    base.publicEndpoint.post
      .tag("Authentication")
      .name("sign-out")
      .summary("Sign out user")
      .in("auth" / "sign-out")
      .in(cookie[RefreshToken]("refreshToken"))
      .out(setCookie("refreshToken"))

object AuthorizationEndpoints:
  val live: URLayer[BaseEndpoints, AuthorizationEndpoints] =
    ZLayer.fromFunction(new AuthorizationEndpoints(_))
