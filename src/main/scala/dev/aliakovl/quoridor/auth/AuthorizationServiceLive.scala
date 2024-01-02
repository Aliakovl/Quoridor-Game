package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.AuthException.InvalidAccessToken
import dev.aliakovl.quoridor.auth.model.*
import io.circe.generic.auto.*
import io.circe.parser
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim, JwtOptions}
import zio.Clock.javaClock
import zio.{IO, ZIO}
import zio.ZIO.ifZIO

import java.security.interfaces.RSAPublicKey

class AuthorizationServiceLive(
    publicKey: RSAPublicKey
) extends AuthorizationService:
  override def validate(
      accessToken: AccessToken
  ): IO[InvalidAccessToken, ClaimData] =
    getClaims(accessToken)
      .flatMap { case (claim, payload) =>
        ifZIO(javaClock.map(implicit clock => payload.isValid))(
          ZIO.succeed(claim),
          ZIO.fail(InvalidAccessToken)
        )
      }

  private[auth] override def verifySign(
      accessToken: AccessToken
  ): IO[InvalidAccessToken, ClaimData] =
    getClaims(accessToken).map(_._1)

  private def getClaims(
      accessToken: AccessToken
  ): IO[InvalidAccessToken, (ClaimData, JwtClaim)] = {
    for {
      payload <- ZIO.fromTry(
        JwtCirce.decode(
          accessToken.value,
          publicKey,
          Seq(JwtAlgorithm.RS256),
          JwtOptions(expiration = false)
        )
      )
      claim <- ZIO.fromEither(
        parser.parse(payload.content).flatMap(_.as[ClaimData])
      )
    } yield (claim, payload)
  }.orElseFail(InvalidAccessToken)
