package ru.quoridor.auth.store

import io.lettuce.core.codec.{RedisCodec, StringCodec}
import ru.quoridor.auth.model.RefreshToken
import ru.quoridor.model.User
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.*

import java.nio.ByteBuffer
import java.util.UUID

package object redis {
  implicit object UUIDRedisCodec extends RedisCodec[RefreshToken, ID[User]] {
    override def decodeValue(bytes: ByteBuffer): ID[User] =
      UUID.fromString(stringCodec.decodeKey(bytes)).tag[User]

    override def decodeKey(bytes: ByteBuffer): RefreshToken =
      RefreshToken(stringCodec.decodeKey(bytes))

    override def encodeValue(key: ID[User]): ByteBuffer =
      stringCodec.encodeKey(key.toString)

    override def encodeKey(value: RefreshToken): ByteBuffer =
      stringCodec.encodeKey(value.value)

    private val stringCodec: StringCodec = StringCodec.UTF8
  }
}
