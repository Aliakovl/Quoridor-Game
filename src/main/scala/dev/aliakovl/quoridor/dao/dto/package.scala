package dev.aliakovl.quoridor.dao

import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.quoridor.engine
import dev.aliakovl.quoridor.model.User

package object dto:
  type UserId = ID[User]
  type GameId = ID[engine.Game]
