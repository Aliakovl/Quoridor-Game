package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.{AccessToken, ClaimData}
import dev.aliakovl.quoridor.config.{Auth, Configuration, TokenKeys}
import dev.aliakovl.utils.RSAKeyReader
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import zio.Clock.javaClock
import zio.nio.file.Path
import zio.{Duration, RLayer, TaskLayer, UIO, ZIO, ZLayer}

import java.security.interfaces.RSAPrivateKey
import java.time.Clock

class AccessServiceLive(
    privateKey: RSAPrivateKey,
    ttl: Duration
) extends AccessService:
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

object AccessServiceLive:
  val live: RLayer[TokenKeys & Auth, AccessService] = ZLayer {
    for {
      tokenKeys <- ZIO.service[TokenKeys]
      ttl <- ZIO.service[Auth]
      privateKey <- RSAKeyReader.readPrivateKey(
        Path(tokenKeys.privateKeyPath)
      )
    } yield new AccessServiceLive(privateKey, ttl.ttl)
  }

  val configuredLive: TaskLayer[AccessService] =
    Configuration.auth ++ Configuration.tokenKeys >>> live
