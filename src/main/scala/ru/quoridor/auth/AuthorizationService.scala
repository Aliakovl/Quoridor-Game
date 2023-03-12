package ru.quoridor.auth

import io.circe.generic.auto._
import io.circe.parser
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtTime}
import ru.quoridor.auth.model.{AccessToken, ClaimData}
import ru.quoridor.config.TokenKeys
import ru.utils.RSAKeyReader
import zio.Clock.javaClock
import zio.nio.file.Path
import zio.{RLayer, Task, ZIO, ZLayer}

import java.security.interfaces.RSAPublicKey

trait AuthorizationService {
  def verified(accessToken: AccessToken): Task[ClaimData]
}

object AuthorizationService {
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
