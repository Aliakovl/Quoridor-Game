package ru.quoridor.services

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import doobie.Transactor
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import ru.quoridor.app.AppConfig
import ru.quoridor.storage.{GameStorage, ProtoGameStorage, UserStorage}
import ru.quoridor.storage.sqlStorage.{GameStorageImpl, ProtoGameStorageImpl, UserStorageImpl}

class GameCreatorSpec extends AsyncFlatSpec
  with AsyncIOSpec
  with Matchers {

  private val appConfig = ConfigSource.default.loadOrThrow[AppConfig]

  private val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    appConfig.DB.driver,
    appConfig.DB.url,
    appConfig.DB.user,
    appConfig.DB.password
  )

  private val userStorage: UserStorage[IO] = new UserStorageImpl[IO](Resource.pure[IO, Transactor[IO]](xa))
  private val protoGameStorage: ProtoGameStorage[IO] = new ProtoGameStorageImpl[IO](Resource.pure[IO, Transactor[IO]](xa))
  private val gameStorage: GameStorage[IO] = new GameStorageImpl[IO](Resource.pure[IO, Transactor[IO]](xa))


  behavior of "startGame"

  it should "start a new game by creator" in {
    IO(succeed)
  }

}
