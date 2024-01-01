package dev.aliakovl.utils.tagging

import dev.aliakovl.utils.tagging.Tagged.*
import io.circe.{Decoder, Encoder}
import sttp.tapir.*

case class Tagged[+A, B](untag: A) extends AnyVal:
  override def toString: String = untag.toString

object Tagged:
  extension [A](a: A) inline def tag[B]: A @@ B = Tagged(a)

  given [A, B](using ev: Decoder[A]): Decoder[A @@ B] = ev.map(_.tag[B])

  given [A, B](using ev: Encoder[A]): Encoder[A @@ B] = ev.contramap(_.untag)

  given [A, B](using ev: Schema[A]): Schema[A @@ B] = ev.as

  given [A, B, L, CF <: CodecFormat](using
      ev: Codec[L, A, CF]
  ): Codec[L, A @@ B, CF] = ev.map(_.tag[B])(_.untag)
