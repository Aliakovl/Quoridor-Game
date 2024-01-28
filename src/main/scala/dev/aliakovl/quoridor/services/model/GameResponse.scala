package dev.aliakovl.quoridor.services.model

import dev.aliakovl.quoridor.model.*
import dev.aliakovl.utils.tagging.ID
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class GameResponse(
    id: ID[Game],
    step: Int,
    state: State,
    winner: Option[User]
)

object GameResponse:
  given Encoder[GameResponse] = deriveEncoder
  given Decoder[GameResponse] = deriveDecoder
  given Schema[GameResponse] = Schema.derivedSchema[GameResponse]
