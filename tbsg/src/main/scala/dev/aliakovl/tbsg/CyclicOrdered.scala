package dev.aliakovl.tbsg

import cats.data.NonEmptySet

import scala.math.Ordering.Implicits.*

trait CyclicOrdered[F[_]]:
  extension [A](set: F[A]) def clockwise(value: A)(using Ordering[A]): Option[A]

object CyclicOrdered:
  given CyclicOrdered[NonEmptySet] with
    extension [A](set: NonEmptySet[A])
      override def clockwise(value: A)(using Ordering[A]): Option[A] = {
        set.filter(_ > value).minOption match {
          case None => (set - value).minOption
          case min  => min
        }
      }
end CyclicOrdered
