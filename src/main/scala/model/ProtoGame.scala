package model

import java.util.UUID

case class ProtoGame(gameId: UUID, users: Seq[User])
