package dev.aliakovl.quoridor.dao

import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.quoridor.model

package object dto:
  type UserId = ID[model.User]
  type GameId = ID[model.Game]
