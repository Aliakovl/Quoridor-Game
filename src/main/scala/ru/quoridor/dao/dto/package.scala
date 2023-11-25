package ru.quoridor.dao

import ru.utils.tagging.ID
import ru.quoridor.engine.model
import ru.quoridor.engine.model.game

package object dto:
  type UserId = ID[model.User]
  type GameId = ID[game.Game]
