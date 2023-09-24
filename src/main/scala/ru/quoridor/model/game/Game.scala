package ru.quoridor.model.game

import io.circe.*
import io.circe.generic.semiauto.*
import sttp.tapir.generic.auto.*
import ru.quoridor.model.User
import ru.utils.tagging.ID
import sttp.tapir.Schema

case class Game(id: ID[Game], step: Int, state: State, winner: Option[User])

object Game:
  given Encoder[Game] = deriveEncoder
  given Decoder[Game] = deriveDecoder
  given Schema[Game] = Schema.derivedSchema[Game]
