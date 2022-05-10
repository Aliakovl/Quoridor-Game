package model

import cats.data.NonEmptyList
import model.game.Game
import model.game.geometry.Side
import utils.Typed.ID

case class ProtoGame(gameId: ID[Game], users: NonEmptyList[ProtoPlayer])

case class ProtoPlayer(userId: ID[User], login: String, target: Side)

//case class ProtoGame(gameId: ID[Game], protoPlayer: ProtoPlayers)

//case class ProtoPlayers(creator: ProtoPlayer, users: NonEmptyList[ProtoPlayer])