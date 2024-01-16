package dev.aliakovl.quoridor.app

import dev.aliakovl.quoridor.config.{Configuration, SSLKeyStore}
import dev.aliakovl.utils.SSLProvider
import zio.nio.file.Path
import zio.{RLayer, TaskLayer, ZIO, ZLayer}

import javax.net.ssl.SSLContext

object SSLProviderLive extends SSLProvider:
  val live: RLayer[SSLKeyStore, SSLContext] = ZLayer(
    ZIO.serviceWithZIO[SSLKeyStore] { case SSLKeyStore(path, password) =>
      apply(Path(path), password.toCharArray)
    }
  )

  val configuredLive: TaskLayer[SSLContext] = Configuration.sslKeyStore >>> live
