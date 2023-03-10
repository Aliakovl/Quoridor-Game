package ru.quoridor.auth

import io.circe.generic.auto._
import io.circe.parser
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtTime}
import ru.quoridor.auth.model.{AccessToken, ClaimData}
import zio.Clock.javaClock
import zio.{Task, ZIO}

import java.security.PublicKey

trait AuthorizationService {
  def verified(accessToken: AccessToken): Task[ClaimData]
}

class AuthorizationServiceImpl(publicKey: PublicKey)
    extends AuthorizationService {
  override def verified(accessToken: AccessToken): Task[ClaimData] = ZIO
    .fromTry {
      JwtCirce.decode(accessToken.value, publicKey, Seq(JwtAlgorithm.RS256))
    }
    .tap(payload =>
      ZIO
        .succeed(payload.expiration)
        .someOrFail(new Throwable("no expiration"))
        .flatMap(nonExpired)
    )
    .flatMap { payload =>
      ZIO.fromEither(parser.parse(payload.content))
    }
    .flatMap { json =>
      ZIO.fromEither(json.as[ClaimData])
    }

  private def nonExpired(expiration: Long): ZIO[Any, Throwable, Long] = {
    javaClock
      .map { implicit clock =>
        JwtTime.nowSeconds
      }
      .filterOrFail(_ < expiration)(new Throwable("expired access token"))
  }
}
