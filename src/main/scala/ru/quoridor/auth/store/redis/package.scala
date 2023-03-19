package ru.quoridor.auth.store

import io.lettuce.core.codec.{RedisCodec, StringCodec}
import ru.quoridor.auth.model.{RefreshToken, TokenSignature}

import java.nio.ByteBuffer

package object redis {
  implicit object UUIDRedisCodec
      extends RedisCodec[RefreshToken, TokenSignature] {
    override def decodeValue(bytes: ByteBuffer): TokenSignature =
      TokenSignature(stringCodec.decodeKey(bytes))

    override def decodeKey(bytes: ByteBuffer): RefreshToken =
      RefreshToken(stringCodec.decodeKey(bytes))

    override def encodeValue(key: TokenSignature): ByteBuffer =
      stringCodec.encodeKey(key.value)

    override def encodeKey(value: RefreshToken): ByteBuffer =
      stringCodec.encodeKey(value.value)

    private val stringCodec: StringCodec = StringCodec.UTF8
  }
}
