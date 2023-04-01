package ru.utils

import zio.{Task, ZIO}
import zio.nio.file.{Files, Path}

import java.security.KeyFactory
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

object RSAKeyReader {
  def readPublicKey(path: Path): Task[RSAPublicKey] = for {
    publicKeyPEM <- Files
      .lines(path)
      .filterNot(_.startsWith("-----BEGIN PUBLIC KEY-----"))
      .filterNot(_.startsWith("-----END PUBLIC KEY-----"))
      .map(_.replaceAll(System.lineSeparator(), ""))
      .mkString
    encoded <- ZIO.attempt(Base64.getDecoder.decode(publicKeyPEM))
    keySpec <- ZIO.attempt(new X509EncodedKeySpec(encoded))
    publicKey <- ZIO.attempt(
      KeyFactory
        .getInstance("RSA")
        .generatePublic(keySpec)
        .asInstanceOf[RSAPublicKey]
    )
  } yield publicKey

  def readPrivateKey(path: Path): Task[RSAPrivateKey] = for {
    privateKeyPEM <- Files
      .lines(path)
      .filterNot(_.startsWith("-----BEGIN PRIVATE KEY-----"))
      .filterNot(_.startsWith("-----END PRIVATE KEY-----"))
      .map(_.replaceAll(System.lineSeparator(), ""))
      .mkString
    encoded <- ZIO.attempt(Base64.getDecoder.decode(privateKeyPEM))
    keySpec <- ZIO.attempt(new PKCS8EncodedKeySpec(encoded))
    privateKey <- ZIO.attempt(
      KeyFactory
        .getInstance("RSA")
        .generatePrivate(keySpec)
        .asInstanceOf[RSAPrivateKey]
    )
  } yield privateKey
}
