package ru.quoridor.auth

import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import ru.quoridor.auth.model.{AccessToken, ClaimData}
import ru.utils.RSAKeyReader
import zio.Clock.javaClock
import zio.{TaskLayer, UIO, ZIO, ZLayer, durationInt}

import java.nio.file.Path
import java.security.interfaces.RSAPrivateKey
import java.time.Clock

trait AccessService {
  def generateToken(claimData: ClaimData): UIO[AccessToken]
}

object AccessService {
  val live: TaskLayer[AccessService] = ZLayer(
    for {
      privateKey <- ZIO.attempt(
        RSAKeyReader.readPrivateKey(Path.of("/var/keys/jwtRSA256.pem").toFile)
      )
    } yield new AccessServiceImpl(privateKey)
  )
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
      "username" -> claimData.username
    )
}
