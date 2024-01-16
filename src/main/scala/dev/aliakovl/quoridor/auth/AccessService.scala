package dev.aliakovl.quoridor.auth

import dev.aliakovl.quoridor.auth.model.{AccessToken, ClaimData}
import zio.*

trait AccessService:
  def generateToken(claimData: ClaimData): UIO[AccessToken]
