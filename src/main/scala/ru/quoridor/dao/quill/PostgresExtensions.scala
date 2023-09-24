package ru.quoridor.dao.quill

import io.getquill.MappedEncoding
import io.getquill.context.jdbc.JdbcContextTypes
import org.postgresql.util.PGobject
import ru.quoridor.model.game.geometry.{Orientation, Side}
import ru.utils.tagging.Tagged
import ru.utils.tagging.Tagged.*

import java.sql.Types

trait PostgresExtensions:
  this: JdbcContextTypes[_, _] =>
  given Encoder[Side] =
    encoder[Side](
      Types.OTHER,
      (index: Index, value: Side, row: PrepareRow) => {
        val pgObj = new PGobject()
        pgObj.setType("side")
        pgObj.setValue(value.entryName)
        row.setObject(index, pgObj, Types.OTHER)
      }
    )

  given Decoder[Side] =
    decoder[Side] { row => index =>
      Side.withName(row.getObject(index, classOf[String]))
    }

  given Encoder[Orientation] =
    encoder[Orientation](
      Types.OTHER,
      (index: Index, value: Orientation, row: PrepareRow) => {
        val pgObj = new PGobject()
        pgObj.setType("orientation")
        pgObj.setValue(value.entryName)
        row.setObject(index, pgObj, Types.OTHER)
      }
    )

  given Decoder[Orientation] =
    decoder[Orientation] { row => index =>
      Orientation.withName(row.getObject(index, classOf[String]))
    }

  given [A, B](using Encoder[A]): Encoder[Tagged[A, B]] =
    given MappedEncoding[Tagged[A, B], A] = MappedEncoding(_.untag)
    mappedEncoder[Tagged[A, B], A]

  given [A, B](using Decoder[A]): Decoder[Tagged[A, B]] =
    given MappedEncoding[A, Tagged[A, B]] = MappedEncoding(_.tag)
    mappedDecoder[A, Tagged[A, B]]
