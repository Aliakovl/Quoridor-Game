package dev.aliakovl.utils

import cats.data.NonEmptyList
import scala.collection.mutable.ListBuffer
import scala.math.Ordering.Implicits.*

case class Shifting[+T](el: T, ar: NonEmptyList[T]):
  def shift[TT >: T](using ord: Ordering[TT]): Shifting[TT] = ar match {
    case NonEmptyList(h, t) =>
      val buf = ListBuffer.empty[TT]
      buf.addOne(el)
      val newEl = t.foldLeft(h) { (head, elem) =>
        val s = buf.head
        if (evenPermutation(head, elem, s)) {
          buf.addOne(elem)
          head
        } else {
          buf.addOne(head)
          elem
        }
      }
      Shifting(newEl, NonEmptyList.fromListUnsafe(buf.toList))
  }

  private def evenPermutation[TT >: T](x: TT, y: TT, z: TT)(using
      ord: Ordering[TT]
  ): Boolean = {
    evenOrder(x, y, z) || evenOrder(y, z, x) || evenOrder(z, x, y)
  }

  private def evenOrder[TT >: T](x: TT, y: TT, z: TT)(using
      ord: Ordering[TT]
  ): Boolean = {
    x <= y && y <= z
  }
