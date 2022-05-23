package ru.quoridor.starage

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import doobie.Transactor
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import ru.quoridor.GameException.{GameNotFoundException, SamePlayerException, UserNotFoundException}
import ru.quoridor.{ProtoPlayer, User}
import ru.quoridor.app.AppConfig
import ru.quoridor.game.Game
import ru.quoridor.game.geometry.Side.{North, South}
import ru.quoridor.storage.{ProtoGameStorage, UserStorage}
import ru.quoridor.storage.sqlStorage.{ProtoGameStorageImpl, UserStorageImpl}
import ru.utils.Typed.Implicits.TypedOps

import java.util.UUID

class ProtoGameStorageSpec extends AnyFlatSpec
  with Matchers
  with BeforeAndAfterAll {

  private val appConfig = ConfigSource.default.loadOrThrow[AppConfig]

  private val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    appConfig.DB.driver,
    appConfig.DB.url,
    appConfig.DB.user,
    appConfig.DB.password
  )

  private val userStorage: UserStorage[IO] = new UserStorageImpl[IO](Resource.pure[IO, Transactor[IO]](xa))

  private val protoGameStorage: ProtoGameStorage[IO] = new ProtoGameStorageImpl[IO](Resource.pure[IO, Transactor[IO]](xa))


  private val userLogin = "user_" + "ProtoGameStorageSpec"
  private val otherUserLogin = "other_user_" + "ProtoGameStorageSpec"

  behavior of "insert"

  it should "create a proto game with user as a creator" in {
    for {
      user <- userStorage.insert(userLogin)
      protoGame <- protoGameStorage.insert(user.id)
      id = protoGame.players.creator.id
      login = protoGame.players.creator.login
      target = protoGame.players.creator.target
      _ = target shouldEqual North
    } yield User(id, login) shouldEqual user
  }.unsafeRunSync()

  it should "not create a game for unknown user" in {
    protoGameStorage.insert(UUID.randomUUID().typed[User]).map(_ => fail).handleError {
      case _: UserNotFoundException => succeed
    }
  }.unsafeRunSync()

  it should "create a new game all the time" in {
    for {
      user <- userStorage.findByLogin(userLogin)
      protoGame <- protoGameStorage.insert(user.id)
      otherProtoGame <- protoGameStorage.insert(user.id)
      _ = protoGame.players shouldEqual otherProtoGame.players
    } yield protoGame.id should not equal otherProtoGame.id
  }.unsafeRunSync()


  behavior of "find"

  it should "find created proto game" in {
    for {
      user <- userStorage.findByLogin(userLogin)
      protoGame <- protoGameStorage.insert(user.id)
      theSameProtoGame <- protoGameStorage.find(protoGame.id)
    } yield theSameProtoGame shouldEqual protoGame
  }.unsafeRunSync()

  it should "not find not created game" in {
    protoGameStorage.find(UUID.randomUUID().typed[Game]).map(_ => fail).handleError {
      case _: GameNotFoundException => succeed
    }
  }.unsafeRunSync()


  behavior of "update"

  it should "add a new player into a proto game" in {
    for {
      user <- userStorage.findByLogin(userLogin)
      newUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, newUser.id, South)
      _ = updatedProtoGame.players.creator shouldEqual protoGame.players.creator
      _ = updatedProtoGame.players.guests.map { case ProtoPlayer(id, login, _) =>
        User(id, login) shouldEqual newUser
      }
    } yield updatedProtoGame.id shouldEqual protoGame.id
  }.unsafeRunSync()

  it should "not add the same player into a proto game twice" in {
    val test = for {
      user <- userStorage.findByLogin(userLogin)
      protoGame <- protoGameStorage.insert(user.id)
      _ <- protoGameStorage.update(protoGame.id, user.id, South)
    } yield ()

    test.map(_ => fail).handleError {
      case _: SamePlayerException => succeed
    }
  }.unsafeRunSync()

  it should "not add a player with the same target into a proto game" in {
    val test = for {
      user <- userStorage.findByLogin(userLogin)
      newUser <- userStorage.findByLogin(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      _ <- protoGameStorage.update(protoGame.id, newUser.id, North)
    } yield ()

    test.map(_ => fail).handleError {
      case _: SamePlayerException => succeed
    }
  }.unsafeRunSync()

  it should "not add an unknown player into a proto game" in {
    val test = for {
      user <- userStorage.findByLogin(userLogin)
      protoGame <- protoGameStorage.insert(user.id)
      _ <- protoGameStorage.update(protoGame.id, UUID.randomUUID().typed[User], South)
    } yield ()

    test.map(_ => fail).handleError {
      case _: UserNotFoundException => succeed
    }
  }.unsafeRunSync()

  it should "not add a new player into an unknown proto game" in {
    val test = for {
      user <- userStorage.findByLogin(userLogin)
      _ <- protoGameStorage.update(UUID.randomUUID().typed[Game], user.id, South)
    } yield ()

    test.map(_ => fail).handleError {
      case _: GameNotFoundException => succeed
    }
  }.unsafeRunSync()
}
