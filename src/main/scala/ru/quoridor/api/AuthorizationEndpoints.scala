package ru.quoridor.api

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir.*
import io.circe.generic.auto.*
import ru.quoridor.auth.model.{AccessToken, Credentials, RefreshToken}
import sttp.model.StatusCode
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.Endpoint
import zio.ZLayer

class AuthorizationEndpoints(base: BaseEndpoints):
  val singUpEndpoint: Endpoint[
    Unit,
    Credentials,
    Throwable,
    (AccessToken, CookieValueWithMeta),
    Any
  ] =
    base.publicEndpoint.post
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
      .in("auth" / "refresh")
      .in(cookie[RefreshToken]("refreshToken"))
      .out(plainBody[AccessToken])
      .out(setCookie("refreshToken"))

  val signOutEndpoint
      : Endpoint[Unit, RefreshToken, Throwable, CookieValueWithMeta, Any] =
    base.publicEndpoint.post
      .in("auth" / "sign-out")
      .in(cookie[RefreshToken]("refreshToken"))
      .out(setCookie("refreshToken"))

object AuthorizationEndpoints:
  val live: ZLayer[BaseEndpoints, Nothing, AuthorizationEndpoints] =
    ZLayer.fromFunction(new AuthorizationEndpoints(_))
