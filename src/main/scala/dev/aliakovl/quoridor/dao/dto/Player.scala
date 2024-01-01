package dev.aliakovl.quoridor.dao.dto

import dev.aliakovl.quoridor.model.game.geometry.Side

case class Player(gameId: GameId, userId: UserId, target: Side)
