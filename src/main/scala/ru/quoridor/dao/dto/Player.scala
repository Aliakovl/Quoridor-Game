package ru.quoridor.dao.dto

import ru.quoridor.model.game.geometry.Side

case class Player(gameId: GameId, userId: UserId, target: Side)
