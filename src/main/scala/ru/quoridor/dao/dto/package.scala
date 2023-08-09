package ru.quoridor.dao

import ru.utils.tagging.ID
import ru.quoridor.model.{User, game}

package object dto {
  type UserId = ID[User]
  type GameId = ID[game.Game]
}
