package dev.aliakovl.utils.tapir

import cats.syntax.functor._
import io.circe.syntax.given
import io.circe.generic.auto._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

enum StreamMessage[+E, +A]:
  case Message(message: A) extends StreamMessage[Nothing, A]
  case Error(error: E) extends StreamMessage[E, Nothing]
  case Ping extends StreamMessage[Nothing, Nothing]

object StreamMessage:
  type Ping = Ping.type
  given [E: Encoder, A: Encoder]: Encoder[StreamMessage[E, A]] =
    Encoder.instance {
      case message @ StreamMessage.Message(_) => message.asJson
      case error @ StreamMessage.Error(_)     => error.asJson
      case StreamMessage.Ping                 => Json.fromString("ping")
    }
  given [E: Decoder, A: Decoder]: Decoder[StreamMessage[E, A]] =
    List[Decoder[StreamMessage[E, A]]](
      Decoder[Message[E, A]].widen,
      Decoder[Error[E, A]].widen,
      Decoder[Ping].widen
    ).reduceLeft(_ or _)
