package ru.quoridor

import ru.quoridor.auth._
import ru.quoridor.model.game.Game
import ru.quoridor.services._
import zio.{Hub, RIO}

package object app {
  type Env = GameService
    with GameCreator
    with UserService
    with AuthenticationService
    with AuthorizationService
    with Hub[Game]

  type EnvTask[A] = RIO[Env, A]
}
