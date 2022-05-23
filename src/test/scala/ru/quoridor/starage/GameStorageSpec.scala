package ru.quoridor.starage

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import doobie.Transactor
import doobie.implicits._
import doobie.util.update.Update
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import ru.quoridor.GameException.GameNotFoundException
import ru.quoridor.User
import ru.quoridor.app.AppConfig
import ru.quoridor.game.{Game, Player, State}
import ru.quoridor.game.geometry.Side.South
import ru.quoridor.storage.{GameStorage, ProtoGameStorage, UserStorage}
import ru.quoridor.storage.sqlStorage.{GameStorageImpl, ProtoGameStorageImpl, UserStorageImpl}
import ru.utils.Typed.Implicits.TypedOps

import java.util.UUID
import scala.io.Source

class GameStorageSpec extends AnyFlatSpec
  with Matchers
  with BeforeAndAfterAll {

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    "sa",
    ""
  )

  val appConfig = ConfigSource.default.loadOrThrow[AppConfig]

  val create = Source.fromResource("migrations/V1__Initial.sql").getLines().mkString("\n")

  override protected def beforeAll(): Unit = {
    Update(create).run().transact(xa).unsafeRunSync()
  }

  val userStorage: UserStorage[IO] = new UserStorageImpl[IO](Resource.pure[IO, Transactor[IO]](xa))

  val protoGameStorage: ProtoGameStorage[IO] = new ProtoGameStorageImpl[IO](Resource.pure[IO, Transactor[IO]](xa))

  val gameStorage: GameStorage[IO] = new GameStorageImpl[IO](Resource.pure[IO, Transactor[IO]](xa))


  behavior of "create"

  it should "create a game" in {
    for {
      user <- userStorage.insert("user")
      otherUser <- userStorage.insert("other_user")
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      _ = game.winner shouldEqual None
      _ = game.state shouldEqual state
    } yield game.id shouldEqual updatedProtoGame.id
  }.unsafeRunSync()

  it should "not create a game from unknown proto game" in {
    val test = for {
      user <- userStorage.findByLogin("user")
      otherUser <- userStorage.findByLogin("other_user")
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(UUID.randomUUID().typed[Game], state)
    } yield game

    test.handleError {
      case _: GameNotFoundException => succeed
    }
  }.unsafeRunSync()


  behavior of "find"

  it should "find a game by id" in {
    for {
      user <- userStorage.findByLogin("user")
      otherUser <- userStorage.findByLogin("other_user")
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      theSameGame <- gameStorage.find(game.id)
    } yield theSameGame shouldEqual game
  }.unsafeRunSync()

  it should "not find a game by unknown id" in {
    gameStorage.find(UUID.randomUUID().typed[Game]).handleError {
      case _: GameNotFoundException => succeed
    }
  }.unsafeRunSync()


  behavior of "exists"

  it should "return true if a game exists" in {
    for {
      user <- userStorage.findByLogin("user")
      otherUser <- userStorage.findByLogin("other_user")
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      isGameExists <- gameStorage.exists(game.id)
    } yield isGameExists shouldEqual true
  }.unsafeRunSync()

  it should "return false if a game does not exist" in {
    gameStorage.exists(UUID.randomUUID().typed[Game]).map(_ shouldEqual false)
  }.unsafeRunSync()


  behavior of "findParticipants"

  it should "find a game players" in {
    for {
      user <- userStorage.findByLogin("user")
      otherUser <- userStorage.findByLogin("other_user")
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      gamePreView <- gameStorage.findParticipants(game.id)
      _ = gamePreView.id shouldEqual game.id
      _ = gamePreView.winner shouldEqual game.winner
    } yield gamePreView.players.toSet shouldEqual game.state.players.toList.map {
      case Player(id, login, _, _, _) => User(id, login)
    }.toSet
  }.unsafeRunSync()

  it should "not find players for an unknown game" in {
    gameStorage.findParticipants(UUID.randomUUID().typed[Game]).handleError {
      case _: GameNotFoundException => succeed
    }
  }.unsafeRunSync()


  behavior of "insert"

  it should "insert a new game state" in {
    for {
      user <- userStorage.findByLogin("user")
      otherUser <- userStorage.findByLogin("other_user")
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      newGame <- gameStorage.insert(game.id, state, None)
    } yield newGame.id should not equal game
  }.unsafeRunSync()

  it should "not insert a new game " in {
    val test = for {
      user <- userStorage.findByLogin("user")
      otherUser <- userStorage.findByLogin("other_user")
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      newGame <- gameStorage.insert(UUID.randomUUID().typed[Game], state, None)
    } yield newGame

    test.handleError {
      case _: GameNotFoundException => succeed
    }
  }.unsafeRunSync()
}
