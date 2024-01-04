package dev.aliakovl.utils

import dev.aliakovl.quoridor.config.{Configuration, SSLKeyStore}
import zio.*
import zio.nio.file.Path

import java.io.FileInputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext}

object SSLProvider:
  def apply(path: Path, password: Array[Char]): Task[SSLContext] =
    ZIO.attemptBlocking {
      val keyStore = KeyStore.getInstance("PKCS12")
      val in = new FileInputStream(path.toFile)
      keyStore.load(in, password)
      val keyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
      keyManagerFactory.init(keyStore, password)
      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(
        keyManagerFactory.getKeyManagers,
        null,
        new SecureRandom()
      )
      sslContext
    }

  val live: RLayer[SSLKeyStore, SSLContext] = ZLayer(
    ZIO.serviceWithZIO[SSLKeyStore] { case SSLKeyStore(path, password) =>
      SSLProvider(Path(path), password.toCharArray)
    }
  )

  val configuredLive: TaskLayer[SSLContext] = Configuration.sslKeyStore >>> live
