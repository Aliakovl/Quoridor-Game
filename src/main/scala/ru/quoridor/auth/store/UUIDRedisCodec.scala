package ru.quoridor.auth.store

import io.lettuce.core.codec.{RedisCodec, StringCodec}
import ru.quoridor.auth.model.RefreshToken
import ru.quoridor.model.User
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.Implicits.TaggedOps

import java.nio.ByteBuffer
import java.util.UUID

object UUIDRedisCodec extends RedisCodec[ID[User], RefreshToken] {
  override def decodeKey(bytes: ByteBuffer): ID[User] =
    UUID.fromString(stringCodec.decodeKey(bytes)).tag[User]

  override def decodeValue(bytes: ByteBuffer): RefreshToken =
    RefreshToken(UUID.fromString(stringCodec.decodeKey(bytes)))

  override def encodeKey(key: ID[User]): ByteBuffer =
    stringCodec.encodeKey(key.toString)

  override def encodeValue(value: RefreshToken): ByteBuffer =
    stringCodec.encodeKey(value.value.toString)

  private val stringCodec: StringCodec = StringCodec.UTF8
}
