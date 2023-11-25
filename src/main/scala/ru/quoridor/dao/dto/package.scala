package ru.quoridor.dao

import ru.utils.tagging.ID
import ru.quoridor.engine
import ru.quoridor.model.User

package object dto:
  type UserId = ID[User]
  type GameId = ID[engine.Game]
