package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.{AccessToken, ClaimData}
import dev.aliakovl.quoridor.config.{Auth, TokenKeys}
import dev.aliakovl.utils.RSAKeyReader
import zio.nio.file.Path
import zio.*

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
    } yield new AccessServiceLive(privateKey, ttl.ttl)
  }
