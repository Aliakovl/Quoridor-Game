package ru.utils

import java.util.UUID

package object tagging {
  import tagging.Tagged

  type ID[B] = Tagged[UUID, B]
  type @@[A, B] = Tagged[A, B]
}
