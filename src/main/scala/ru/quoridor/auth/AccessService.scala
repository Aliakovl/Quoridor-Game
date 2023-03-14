package ru.quoridor.auth

import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import ru.quoridor.auth.model.{AccessToken, ClaimData}
import ru.quoridor.config.TokenKeys
import ru.utils.RSAKeyReader
import zio.Clock.javaClock
import zio.nio.file.Path
import zio.{RLayer, UIO, ZIO, ZLayer, durationInt}

import java.security.interfaces.RSAPrivateKey
import java.time.Clock

trait AccessService {
  def generateToken(claimData: ClaimData): UIO[AccessToken]
}

object AccessService {
  val live: RLayer[TokenKeys, AccessService] = ZLayer {
    ZIO.serviceWithZIO[TokenKeys] { tokenKeys =>
      for {
        privateKey <- RSAKeyReader.readPrivateKey(
          Path(tokenKeys.`private-key-path`)
        )
      } yield new AccessServiceImpl(privateKey)
    }
  }
}

class AccessServiceImpl(private val privateKey: RSAPrivateKey)
    extends AccessService {
  override def generateToken(claimData: ClaimData): UIO[AccessToken] =
    javaClock.map { implicit clock =>
      val payload = generatePayload(claimData)
      val jwt = JwtCirce(clock).encode(payload, privateKey, JwtAlgorithm.RS256)
      AccessToken(jwt)
    }

  private val ttl: Long = 15.minutes.toSeconds

  private def generatePayload(
      claimData: ClaimData
  )(implicit clock: Clock) = JwtClaim()
    .expiresIn(ttl)
    .++(
      "userId" -> claimData.userId,
      "username" -> claimData.username.value
    )
}
