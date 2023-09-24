package ru.utils.tagging

import io.circe.{Decoder, Encoder}
import io.getquill.context.jdbc.Encoders
import ru.quoridor.model.User
import ru.utils.tagging.Tagged.*
import sttp.tapir.{Codec, CodecFormat, Schema}
import zio.Tag

opaque type Tagged[+A, B] = A

object Tagged:
  extension[A](a: A)
    inline def tag[B]: A @@ B = a

  extension[A, B](t: A @@ B)
    inline def untag: A = t

  given[A, B](using ev: Decoder[A]): Decoder[A @@ B] = ev
  given[A, B](using ev: Encoder[A]): Encoder[A @@ B] = ev
  given[A, B](using ev: Schema[A]): Schema[A @@ B] = ev.as

  given[A, B, L, CF <: CodecFormat](using
      ev: Codec[L, A, CF]
  ): Codec[L, A @@ B, CF] = ev

  given[A: Tag, B]: Tag[Tagged[A, B]] = Tag[A]
