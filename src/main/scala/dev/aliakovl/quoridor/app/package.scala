package dev.aliakovl.quoridor

import dev.aliakovl.quoridor.api.GameApiService
import dev.aliakovl.quoridor.auth.*
import dev.aliakovl.quoridor.services.*
import zio.RIO

package object app:
  type Env = GameService
    with GameCreator
    with UserService
    with GameApiService
    with AuthenticationService
    with AuthorizationService

  type EnvTask[+A] = RIO[Env, A]
