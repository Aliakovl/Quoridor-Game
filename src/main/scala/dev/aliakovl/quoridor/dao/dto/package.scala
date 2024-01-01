package dev.aliakovl.quoridor.dao

import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.quoridor.model
import dev.aliakovl.quoridor.model.game

package object dto:
  type UserId = ID[model.User]
  type GameId = ID[game.Game]
