package ru.quoridor.auth

import io.circe.generic.auto._
import io.circe.parser
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim, JwtOptions}
import ru.quoridor.auth.model.AuthException.InvalidAccessToken
import ru.quoridor.auth.model.{AccessToken, ClaimData, InvalidAccessToken}
import ru.quoridor.config.TokenKeys
import ru.utils.RSAKeyReader
import zio.Clock.javaClock
import zio.ZIO.ifZIO
import zio.nio.file.Path
import zio._

import java.security.interfaces.RSAPublicKey

trait AuthorizationService {
  def validate(accessToken: AccessToken): IO[InvalidAccessToken, ClaimData]

  private[auth] def verifySign(
      accessToken: AccessToken
  ): IO[InvalidAccessToken, ClaimData]
}

object AuthorizationService {
  def validate(
      accessToken: AccessToken
  ): RIO[AuthorizationService, ClaimData] =
    ZIO.serviceWithZIO[AuthorizationService](_.validate(accessToken))

  val live: RLayer[TokenKeys, AuthorizationService] = ZLayer {
    for {
      tokenKeys <- ZIO.service[TokenKeys]
      publicKey <- RSAKeyReader.readPublicKey(
        Path(tokenKeys.publicKeyPath)
      )
    } yield new AuthorizationServiceImpl(publicKey)
  }
}

class AuthorizationServiceImpl(publicKey: RSAPublicKey)
    extends AuthorizationService {
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
}
