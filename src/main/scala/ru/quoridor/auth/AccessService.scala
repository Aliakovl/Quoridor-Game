package ru.quoridor.auth

import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import ru.quoridor.auth.model.{AccessToken, ClaimData}
import ru.quoridor.config.{Auth, TokenKeys}
import ru.utils.RSAKeyReader
import zio.Clock.javaClock
import zio.nio.file.Path
import zio.*

import java.security.interfaces.RSAPrivateKey
import java.time.Clock

trait AccessService:
  def generateToken(claimData: ClaimData): UIO[AccessToken]

object AccessService:
  val live: RLayer[TokenKeys & Auth, AccessService] = ZLayer {
    for {
      tokenKeys <- ZIO.service[TokenKeys]
      ttl <- ZIO.service[Auth]
      privateKey <- RSAKeyReader.readPrivateKey(
        Path(tokenKeys.privateKeyPath)
      )
    } yield new AccessServiceImpl(privateKey, ttl.ttl)
  }

class AccessServiceImpl(private val privateKey: RSAPrivateKey, ttl: Duration)
    extends AccessService:
  override def generateToken(claimData: ClaimData): UIO[AccessToken] =
    javaClock.map { implicit clock =>
      val payload = generatePayload(claimData)
      val jwt = JwtCirce(clock).encode(payload, privateKey, JwtAlgorithm.RS256)
      AccessToken(jwt)
    }

  private val ttlSeconds: Long = ttl.toSeconds

  private def generatePayload(
      claimData: ClaimData
  )(using clock: Clock) = JwtClaim()
    .expiresIn(ttlSeconds)
    .++(
      "userId" -> claimData.userId,
      "username" -> claimData.username.value
    )
