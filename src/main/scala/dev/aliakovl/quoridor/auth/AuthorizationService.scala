package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.*
import zio.*

trait AuthorizationService:
  def validate(accessToken: AccessToken): IO[InvalidAccessToken, ClaimData]

  private[auth] def verifySign(
      accessToken: AccessToken
  ): IO[InvalidAccessToken, ClaimData]
