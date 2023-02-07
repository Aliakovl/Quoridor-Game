package ru.utils

import io.circe.{Decoder, Encoder}
import sttp.tapir.{Codec, CodecFormat, Schema}

import java.util.UUID

case class Tagged[+A, B](untag: A) extends AnyVal {
  override def toString: String = untag.toString
}

object Tagged {
  object Implicits {
    implicit class TaggedOps[A](private val v: A) extends AnyVal {
      def tag[B]: Tagged[A, B] = Tagged[A, B](v)
    }
  }

  type ID[T] = Tagged[UUID, T]
  type @@[A, B] = Tagged[A, B]

  import Tagged.Implicits._

  implicit def decoder[A, B](implicit ev: Decoder[A]): Decoder[A @@ B] =
    ev.map(_.tag[B])
  implicit def encoder[A, B](implicit ev: Encoder[A]): Encoder[A @@ B] =
    ev.contramap(_.untag)
  implicit def schema[A, B](implicit ev: Schema[A]): Schema[A @@ B] =
    ev.as

  implicit def сodec[A, B, L, CF <: CodecFormat](implicit
      ev: Codec[L, A, CF]
  ): Codec[L, A @@ B, CF] =
    ev.map(_.tag[B])(_.untag)
}
