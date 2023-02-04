package ru.quoridor.storage

import doobie.Transactor
import doobie.hikari.HikariTransactor
import ru.quoridor.app.AppConfig
import zio.interop.catz._
import zio.{Scope, Task, ZIO, ZLayer}

class DataBase(appConfig: AppConfig) {
  private val resourceTransactor: ZIO[Scope, Throwable, Transactor[Task]] =
    for {
      ec <- ZIO.blockingExecutor.map(_.asExecutionContext)
      xa <- HikariTransactor
        .newHikariTransactor[Task](
          appConfig.DB.driver,
          appConfig.DB.url,
          appConfig.DB.user,
          appConfig.DB.password,
          ec
        )
        .toScopedZIO
    } yield xa

  def transact[A](transact: Transactor[Task] => Task[A]): Task[A] =
    ZIO.scoped {
      resourceTransactor.flatMap(transact)
    }
}

object DataBase {
  def apply(appConfig: AppConfig): DataBase =
    new DataBase(appConfig)

  val live: ZLayer[AppConfig, Nothing, DataBase] =
    ZLayer.fromFunction(DataBase.apply _)
}
