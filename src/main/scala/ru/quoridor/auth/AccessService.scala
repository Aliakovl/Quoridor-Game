package ru.quoridor.auth

import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import ru.quoridor.auth.model.{AccessToken, ClaimData}
import zio.Clock.javaClock
import zio.{UIO, durationInt}

import java.security.PrivateKey
import java.time.Clock

trait AccessService {
  def generateToken(claimData: ClaimData): UIO[AccessToken]
}

class AccessServiceImpl(private val privateKey: PrivateKey)
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
      "username" -> claimData.username
    )
}
