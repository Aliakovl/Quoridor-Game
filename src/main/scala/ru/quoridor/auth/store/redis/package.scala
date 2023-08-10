package ru.quoridor.auth.store

import io.lettuce.core.codec.{RedisCodec, StringCodec}
import ru.quoridor.auth.model.RefreshToken
import ru.quoridor.model.User
import ru.utils.tagging.Id
import ru.utils.tagging.Tagged.Implicits._

import java.nio.ByteBuffer
import java.util.UUID

package object redis {
  implicit object UUIDRedisCodec extends RedisCodec[RefreshToken, Id[User]] {
    override def decodeValue(bytes: ByteBuffer): Id[User] =
      UUID.fromString(stringCodec.decodeKey(bytes)).tag[User]

    override def decodeKey(bytes: ByteBuffer): RefreshToken =
      RefreshToken(stringCodec.decodeKey(bytes))

    override def encodeValue(key: Id[User]): ByteBuffer =
      stringCodec.encodeKey(key.toString)

    override def encodeKey(value: RefreshToken): ByteBuffer =
      stringCodec.encodeKey(value.value)

    private val stringCodec: StringCodec = StringCodec.UTF8
  }
}
