package ru.quoridor.auth

import io.circe.generic.auto._
import io.circe.parser
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import ru.quoridor.auth.model.{AccessToken, ClaimData}
import ru.quoridor.config.TokenKeys
import ru.utils.RSAKeyReader
import zio.Clock.javaClock
import zio.ZIO.ifZIO
import zio.nio.file.Path
import zio.{RIO, RLayer, Task, ZIO, ZLayer}

import java.security.interfaces.RSAPublicKey

trait AuthorizationService {
  def validate(accessToken: AccessToken): Task[ClaimData]

  private[auth] def verifySign(accessToken: AccessToken): Task[ClaimData]
}

object AuthorizationService {
  def validate(
      accessToken: AccessToken
  ): RIO[AuthorizationService, ClaimData] =
    ZIO.serviceWithZIO[AuthorizationService](_.validate(accessToken))

  val live: RLayer[TokenKeys, AuthorizationService] = ZLayer {
    ZIO.serviceWithZIO[TokenKeys] { tokenKeys =>
      for {
        publicKey <- RSAKeyReader.readPublicKey(
          Path(tokenKeys.`public-key-path`)
        )
      } yield new AuthorizationServiceImpl(publicKey)
    }
  }
}

class AuthorizationServiceImpl(publicKey: RSAPublicKey)
    extends AuthorizationService {
  override def validate(accessToken: AccessToken): Task[ClaimData] =
    getClaims(accessToken).flatMap { case (claim, payload) =>
      ifZIO(javaClock.map(implicit clock => payload.isValid))(
        ZIO.succeed(claim),
        ZIO.fail(new Throwable("expired access token"))
      )
    }

  private[auth] override def verifySign(
      accessToken: AccessToken
  ): Task[ClaimData] =
    getClaims(accessToken).map(_._1)

  private def getClaims(
      accessToken: AccessToken
  ): Task[(ClaimData, JwtClaim)] = {
    for {
      payload <- ZIO.fromTry(
        JwtCirce.decode(accessToken.value, publicKey, Seq(JwtAlgorithm.RS256))
      )
      claim <- ZIO.fromEither(
        parser.parse(payload.content).flatMap(_.as[ClaimData])
      )
    } yield (claim, payload)
  }
}
