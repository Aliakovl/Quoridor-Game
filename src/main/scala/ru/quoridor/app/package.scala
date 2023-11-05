package ru.quoridor

import ru.quoridor.auth.*
import ru.quoridor.services.*
import zio.RIO

package object app {
  type Env = GameService
    with GameCreator
    with UserService
    with AuthenticationService
    with AuthorizationService

  type EnvTask[+A] = RIO[Env, A]
}
