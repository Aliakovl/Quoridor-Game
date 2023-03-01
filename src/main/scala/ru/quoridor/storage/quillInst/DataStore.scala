package ru.quoridor.storage.quillInst

import io.getquill.{CompositeNamingStrategy2, Escape, SnakeCase}
import io.getquill.jdbczio.Quill
import org.postgresql.util.PGobject
import ru.quoridor.model.game.geometry.{Orientation, Side}

import java.sql.Types

class DataStore(
    val quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, Escape]]
) {
  import quill._

  implicit val sideEncoder: Encoder[Side] =
    encoder[Side](
      Types.OTHER,
      (index: Index, value: Side, row: PrepareRow) => {
        val pgObj = new PGobject()
        pgObj.setType("side")
        pgObj.setValue(value.entryName)
        row.setObject(index, pgObj, Types.OTHER)
      }
    )

  implicit val sideDecoder: Decoder[Side] =
    decoder[Side]((row: ResultRow) =>
      (index: Index) => Side.withName(row.getObject(index).toString)
    )

  implicit val orientationEncoder: Encoder[Orientation] =
    encoder[Orientation](
      Types.OTHER,
      (index: Index, value: Orientation, row: PrepareRow) => {
        val pgObj = new PGobject()
        pgObj.setType("orientation")
        pgObj.setValue(value.entryName)
        row.setObject(index, pgObj, Types.OTHER)
      }
    )

  implicit val orientationDecoder: Decoder[Orientation] =
    decoder[Orientation]((row: ResultRow) =>
      (index: Index) => Orientation.withName(row.getObject(index).toString)
    )
}
