package ru.quoridor.dao.quill

import io.getquill.context.jdbc.JdbcContextTypes
import org.postgresql.util.PGobject
import ru.quoridor.model.game.geometry.{Orientation, Side}

import java.sql.Types

trait PostgresExtensions { this: JdbcContextTypes[_, _] =>
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
    decoder[Side] { row: ResultRow => index: Int =>
      Side.withName(row.getObject(index, classOf[String]))
    }

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
    decoder[Orientation] { row: ResultRow => index: Int =>
      Orientation.withName(row.getObject(index, classOf[String]))
    }
}
