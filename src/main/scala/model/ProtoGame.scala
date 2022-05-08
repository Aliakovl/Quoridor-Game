package model

import model.game.geometry.Side

import java.util.UUID

case class ProtoGame(gameId: UUID, users: Seq[ProtoPlayer])

case class ProtoPlayer(userId: UUID, login: String, target: Side)
