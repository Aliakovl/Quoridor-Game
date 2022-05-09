package utils

import java.util.UUID

trait Typed[+A, +B] {
  def unType: A
}

object Typed {
  implicit def unType[A, B](t: Typed[A, B]): A = t.unType

  object Implicits {
    implicit class TypedOps[A](private val v: A) extends AnyVal {
      def typed[B]: Typed[A, B] = new Typed[A, B] {
        override def unType: A = v

        override def hashCode(): Int = v.hashCode()

        override def equals(obj: Any): Boolean =
          obj.hashCode() == this.hashCode()

        override def toString: String = v.toString
      }
    }
  }

  type ID[T] = Typed[UUID, T]
}
