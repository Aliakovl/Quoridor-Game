package ru.quoridor.storage.quill

import io.getquill.context.jdbc.{Decoders, Encoders}
import org.postgresql.util.PGobject
import ru.quoridor.model.game.geometry.{Orientation, Side}

import java.sql.{PreparedStatement, ResultSet, Types}

trait PostgresExtensions { this: Encoders with Decoders =>
  implicit val sideEncoder: Encoder[Side] =
    encoder[Side](
      Types.OTHER,
      (index: Int, value: Side, row: PreparedStatement) => {
        val pgObj = new PGobject()
        pgObj.setType("side")
        pgObj.setValue(value.entryName)
        row.setObject(index, pgObj, Types.OTHER)
      }
    )

  implicit val sideDecoder: Decoder[Side] =
    decoder[Side] { row: ResultSet => index: Int =>
      Side.withName(row.getObject(index, classOf[String]))
    }

  implicit val orientationEncoder: Encoder[Orientation] =
    encoder[Orientation](
      Types.OTHER,
      (index: Int, value: Orientation, row: PreparedStatement) => {
        val pgObj = new PGobject()
        pgObj.setType("orientation")
        pgObj.setValue(value.entryName)
        row.setObject(index, pgObj, Types.OTHER)
      }
    )

  implicit val orientationDecoder: Decoder[Orientation] =
    decoder[Orientation] { row: ResultSet => index: Int =>
      Orientation.withName(row.getObject(index, classOf[String]))
    }
}
