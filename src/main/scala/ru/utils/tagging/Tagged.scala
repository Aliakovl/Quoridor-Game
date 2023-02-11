package ru.utils.tagging

import io.circe.{Decoder, Encoder}
import ru.utils.tagging.Tagged.Implicits.TaggedOps
import sttp.tapir.{Codec, CodecFormat, Schema}

case class Tagged[+A, B](untag: A) extends AnyVal {
  override def toString: String = untag.toString
}

object Tagged {
  object Implicits {
    implicit class TaggedOps[A](private val a: A) extends AnyVal {
      def tag[B]: A @@ B = Tagged[A, B](a)
    }
  }

  implicit def decoder[A, B](implicit ev: Decoder[A]): Decoder[A @@ B] =
    ev.map(_.tag[B])
  implicit def encoder[A, B](implicit ev: Encoder[A]): Encoder[A @@ B] =
    ev.contramap(_.untag)
  implicit def schema[A, B](implicit ev: Schema[A]): Schema[A @@ B] =
    ev.as

  implicit def codec[A, B, L, CF <: CodecFormat](implicit
      ev: Codec[L, A, CF]
  ): Codec[L, A @@ B, CF] =
    ev.map(_.tag[B])(_.untag)
}
