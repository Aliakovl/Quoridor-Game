package ru.quoridor.dao.dto

import ru.quoridor.engine.model.game.geometry.Side

case class Player(gameId: GameId, userId: UserId, target: Side)
