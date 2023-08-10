package ru.quoridor.dao

import ru.utils.tagging.Id
import ru.quoridor.model.{User, game}

package object dto {
  type UserId = Id[User]
  type GameId = Id[game.Game]
}
