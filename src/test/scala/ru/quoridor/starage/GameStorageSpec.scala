package ru.quoridor.starage

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import doobie.Transactor
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import ru.quoridor.GameException.GameNotFoundException
import ru.quoridor.app.AppConfig
import ru.quoridor.game.{Game, Player, State}
import ru.quoridor.game.geometry.Side.South
import ru.quoridor.storage.{GameStorage, ProtoGameStorage, UserStorage}
import ru.quoridor.storage.sqlStorage.{GameStorageImpl, ProtoGameStorageImpl, UserStorageImpl}
import ru.utils.Typed.Implicits.TypedOps

import java.util.UUID

class GameStorageSpec extends AsyncFlatSpec
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

  private def userLogin = s"user_${UUID.randomUUID()}"
  private def otherUserLogin = s"other_user_${UUID.randomUUID()}"

  behavior of "create"

  it should "create a game" in {
    for {
      user <- userStorage.insert(userLogin)
      otherUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      _ = game.winner shouldEqual None
      _ = game.state shouldEqual state
    } yield game.id shouldEqual updatedProtoGame.id
  }

  it should "not create a game from unknown proto game" in {
    val test = for {
      user <- userStorage.insert(userLogin)
      otherUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(UUID.randomUUID().typed[Game], state)
    } yield game

    test.map(_ => fail).handleError {
      case _: GameNotFoundException => succeed
    }
  }


  behavior of "find"

  it should "find a game by id" in {
    for {
      user <- userStorage.insert(userLogin)
      otherUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      theSameGame <- gameStorage.find(game.id)
    } yield theSameGame shouldEqual game
  }

  it should "not find a game by unknown id" in {
    gameStorage.find(UUID.randomUUID().typed[Game]).map(_ => fail).handleError {
      case _: GameNotFoundException => succeed
    }
  }


  behavior of "exists"

  it should "return true if a game exists" in {
    for {
      user <- userStorage.insert(userLogin)
      otherUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      isGameExists <- gameStorage.exists(game.id)
    } yield isGameExists shouldEqual true
  }

  it should "return false if a game does not exist" in {
    gameStorage.exists(UUID.randomUUID().typed[Game]).map(_ shouldEqual false)
  }


  behavior of "findParticipants"

  it should "find a game players" in {
    for {
      user <- userStorage.insert(userLogin)
      otherUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      gamePreView <- gameStorage.findParticipants(game.id)
      _ = gamePreView.id shouldEqual game.id
      _ = gamePreView.winner shouldEqual game.winner
    } yield gamePreView.players.toSet shouldEqual game.state.players.toList.map(_.toUser).toSet
  }

  it should "not find players for an unknown game" in {
    gameStorage.findParticipants(UUID.randomUUID().typed[Game]).map(_ => fail).handleError {
      case _: GameNotFoundException => succeed
    }
  }


  behavior of "insert"

  it should "insert a new game state" in {
    for {
      user <- userStorage.insert(userLogin)
      otherUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      newGame <- gameStorage.insert(game.id, state, None)
    } yield newGame.id should not equal game
  }

  it should "not insert a new game " in {
    val test = for {
      user <- userStorage.insert(userLogin)
      otherUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      newGame <- gameStorage.insert(UUID.randomUUID().typed[Game], state, None)
    } yield newGame

    test.map(_ => fail).handleError {
      case _: GameNotFoundException => succeed
    }
  }


  behavior of "gameHistory"

  it should "return history of a game" in {
    for {
      user <- userStorage.insert(userLogin)
      otherUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      newGame <- gameStorage.insert(game.id, state, None)
      gameHistory <- gameStorage.gameHistory(newGame.id)
    } yield gameHistory shouldEqual List(game.id, newGame.id)
  }

  it should "return history of a just started game" in {
    for {
      user <- userStorage.insert(userLogin)
      otherUser <- userStorage.insert(otherUserLogin)
      protoGame <- protoGameStorage.insert(user.id)
      updatedProtoGame <- protoGameStorage.update(protoGame.id, otherUser.id, South)
      players = updatedProtoGame.players.toPlayers.getOrElse(fail)
      state = State(players, Set.empty)
      game <- gameStorage.create(updatedProtoGame.id, state)
      gameHistory <- gameStorage.gameHistory(game.id)
    } yield gameHistory shouldEqual List(game.id)
  }

  it should "not return history an unknown game" in {
    gameStorage.gameHistory(UUID.randomUUID().typed[Game]).map(_ => fail).handleError {
      case _: GameNotFoundException => succeed
    }
  }
}
