package ru.quoridor.services

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import doobie.Transactor
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import ru.quoridor.app.AppConfig
import ru.quoridor.game.State
import ru.quoridor.game.geometry.Side.South
import ru.quoridor.storage.{GameStorage, ProtoGameStorage, UserStorage}
import ru.quoridor.storage.sqlStorage.{GameStorageImpl, ProtoGameStorageImpl, UserStorageImpl}

import java.util.UUID

class UserServiceSpec extends AsyncFlatSpec
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
  private val userService: UserService[IO] = new UserServiceImpl[IO](userStorage, gameStorage)

  private def userLogin = s"user_${UUID.randomUUID()}"
  private def otherUserLogin = s"other_user_${UUID.randomUUID()}"

  behavior of "usersHistory"

  it should "return users history" in {
    for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      state = State(updatedProtoGame.players.toPlayers.getOrElse(fail), Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      userHistory <- userService.usersHistory(user.id)
      otherUserHistory <- userService.usersHistory(otherUser.id)
      _ = userHistory.head shouldEqual game.toGamePreView
      _ = otherUserHistory.head shouldEqual game.toGamePreView
    } yield userHistory shouldEqual otherUserHistory
  }
}
