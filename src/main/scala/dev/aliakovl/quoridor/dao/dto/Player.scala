package dev.aliakovl.quoridor.dao.dto

import dev.aliakovl.quoridor.engine.game.geometry.Side

case class Player(gameId: GameId, userId: UserId, target: Side)
