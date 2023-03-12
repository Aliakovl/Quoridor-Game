package ru.utils

import java.io.File
import java.nio.file.Files
import java.security.KeyFactory
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

object RSAKeyReader {
  def readPublicKey(file: File): RSAPublicKey = {
    val key = new String(Files.readAllBytes(file.toPath))

    val publicKeyPEM = key
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replaceAll(System.lineSeparator(), "")
      .replace("-----END PUBLIC KEY-----", "")

    val encoded = Base64.getDecoder.decode(publicKeyPEM)

    val keyFactory = KeyFactory.getInstance("RSA")

    val keySpec = new X509EncodedKeySpec(encoded)

    keyFactory.generatePublic(keySpec).asInstanceOf[RSAPublicKey]
  }

  def readPrivateKey(file: File): RSAPrivateKey = {
    val key = new String(Files.readAllBytes(file.toPath))

    val privateKeyPEM = key
      .replace("-----BEGIN PRIVATE KEY-----", "")
      .replaceAll(System.lineSeparator(), "")
      .replace("-----END PRIVATE KEY-----", "")

    val encoded = Base64.getDecoder.decode(privateKeyPEM)

    val keyFactory = KeyFactory.getInstance("RSA")

    val keySpec = new PKCS8EncodedKeySpec(encoded)

    keyFactory.generatePrivate(keySpec).asInstanceOf[RSAPrivateKey]
  }
}
