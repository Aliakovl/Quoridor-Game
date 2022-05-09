package model

import model.game.Game
import model.game.geometry.Side
import utils.Typed.ID

case class ProtoGame(gameId: ID[Game], users: Seq[ProtoPlayer])

case class ProtoPlayer(userId: ID[User], login: String, target: Side)
