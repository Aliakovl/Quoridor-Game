package ru.quoridor.services

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import doobie.Transactor
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import ru.quoridor.GameException._
import ru.quoridor.app.AppConfig
import ru.quoridor.game.geometry.Side._
import ru.quoridor.storage.{GameStorage, ProtoGameStorage, UserStorage}
import ru.quoridor.storage.sqlStorage.{GameStorageImpl, ProtoGameStorageImpl, UserStorageImpl}

import java.util.UUID

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

  private val userService: UserService[IO] = new UserServiceImpl[IO](userStorage, gameStorage)
  private val gameCreator: GameCreator[IO] = new GameCreatorImpl[IO](protoGameStorage, gameStorage)

  private def userLogin = s"user_${UUID.randomUUID()}"
  private def otherUserLogin = s"other_user_${UUID.randomUUID()}"
  private def thirdUserLogin = s"third_user_${UUID.randomUUID()}"
  private def fourthUserLogin = s"fourth_user_${UUID.randomUUID()}"
  private def excessUserLogin = s"excess_user_${UUID.randomUUID()}"

  behavior of "joinPlayer"

  it should "be able to join three other players" in {
    for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      thirdUser <- userService.createUser(thirdUserLogin)
      fourthUser <- userService.createUser(fourthUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      _ <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      _ <- gameCreator.joinPlayer(protoGame.id, thirdUser.id)
      updatedProtoGame <- gameCreator.joinPlayer(protoGame.id, fourthUser.id)
      _ = updatedProtoGame.players.creator.toUser shouldEqual user
      _ = updatedProtoGame.players.guests.map(_.toUser).toSet shouldEqual Set(otherUser, thirdUser, fourthUser)
      _ = updatedProtoGame.players.toList.map(_.target).toSet shouldEqual Set(North, South, West, East)
      _ = updatedProtoGame.id shouldEqual protoGame.id
    } yield ()
  }

  it should "not be able to join more then three other players" in {
    val test = for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      thirdUser <- userService.createUser(thirdUserLogin)
      fourthUser <- userService.createUser(fourthUserLogin)
      excessUser <- userService.createUser(excessUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      _ <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      _ <- gameCreator.joinPlayer(protoGame.id, thirdUser.id)
      _ <- gameCreator.joinPlayer(protoGame.id, fourthUser.id)
      _ <- gameCreator.joinPlayer(protoGame.id, excessUser.id)
    } yield ()

    test.map(_ => fail).handleError {
      case PlayersNumberLimitException => succeed
    }
  }


  behavior of "startGame"

  it should "start a new game by creator" in {
    for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      updatedProtoGame <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      game <- gameCreator.startGame(updatedProtoGame.id, user.id)
      _ = game.id shouldEqual protoGame.id
    } yield game.state.players.toList.map(_.toUser).toSet shouldEqual Set(user, otherUser)
  }

  it should "not start a new game by another user" in {
    val test = for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      updatedProtoGame <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      game <- gameCreator.startGame(updatedProtoGame.id, otherUser.id)
    } yield game

    test.map(_ => fail).handleError {
      case _: NotGameCreatorException => succeed
    }
  }

  it should "not start a new game if there is only one player" in {
    val test = for {
      user <- userService.createUser(userLogin)
      protoGame <- gameCreator.createGame(user.id)
      game <- gameCreator.startGame(protoGame.id, user.id)
    } yield game

    test.map(_ => fail).handleError {
      case NotEnoughPlayersException => succeed
    }
  }
}
