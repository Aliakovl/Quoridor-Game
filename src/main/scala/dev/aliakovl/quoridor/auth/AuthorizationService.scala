package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.*
import dev.aliakovl.quoridor.config.{Configuration, TokenKeys}
import dev.aliakovl.utils.RSAKeyReader
import zio.nio.file.Path
import zio.*

trait AuthorizationService:
  def validate(accessToken: AccessToken): IO[InvalidAccessToken, ClaimData]

  private[auth] def verifySign(
      accessToken: AccessToken
  ): IO[InvalidAccessToken, ClaimData]

object AuthorizationService:
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
    } yield new AuthorizationServiceLive(publicKey)
  }

  val configuredLive: TaskLayer[AuthorizationService] =
    Configuration.tokenKeys >>> live
