package utils

import cats.data.NonEmptyList
import scala.collection.mutable.ListBuffer
import scala.math.Ordering.Implicits._


case class Shifting[+T](el: T, ar: NonEmptyList[T]) {
  def shift[TT >: T](implicit ord: Ordering[TT]): Shifting[TT] = {
    ar match {
      case NonEmptyList(h, t) =>
        val buf = ListBuffer.empty[TT]
        buf.addOne(el)
        val newEl = t.foldLeft(h){ (head, elem) =>
          val s = buf.head
          if (rightOrdered(head, elem, s)) {
            buf.addOne(elem)
            head
          } else {
            buf.addOne(head)
            elem
          }
        }
        Shifting(newEl, NonEmptyList.fromListUnsafe(buf.toList))
    }
  }

  private def rightOrdered[TT >: T](x: TT, y: TT, z: TT)(implicit ord: Ordering[TT]): Boolean = {
    ggg(x, y, z) || ggg(y, z, x) || ggg(z, x, y)
  }

  private def ggg[TT >: T](x: TT, y: TT, z: TT)(implicit ord: Ordering[TT]): Boolean = {
    x <= y && y <= z
  }
}