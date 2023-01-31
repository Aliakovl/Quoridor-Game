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
import ru.quoridor.game.geometry.Orientation._
import ru.quoridor.game._
import ru.quoridor.game.geometry._
import ru.quoridor.storage._
import ru.quoridor.storage.sqlStorage._

import java.util.UUID

class GameServiceSpec extends AsyncFlatSpec
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
  private val gameService: GameService[IO] = new GameServiceImpl[IO](gameStorage)

  private def userLogin = s"user_${UUID.randomUUID()}"
  private def otherUserLogin = s"other_user_${UUID.randomUUID()}"
  private def thirdUserLogin = s"third_user_${UUID.randomUUID()}"
  private def fourthUserLogin = s"fourth_user_${UUID.randomUUID()}"


  behavior of "findGame"

  it should "find game" in {
    for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      _ <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      game <- gameCreator.startGame(protoGame.id, user.id)
      theSameGame <- gameService.findGame(game.id)
      alsoTheSameGame <- gameService.findGame(game.id)
      _ = theSameGame shouldEqual game
      _ = alsoTheSameGame shouldEqual game
    } yield ()
  }

  it should "not find a game for an external users" in {
    val test = for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      thirdUser <- userService.createUser(thirdUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      _ <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      game <- gameCreator.startGame(protoGame.id, user.id)
      _ <- gameService.findGame(game.id)
    } yield ()

    test.map(_ => fail).handleError {
      case _: GameInterloperException => succeed
    }
  }

  it should "make pawn move" in {
    for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      _ <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      game <- gameCreator.startGame(protoGame.id, user.id)
      updatedGame <- gameService.makeMove(game.id, game.state.players.activePlayer.id, PawnMove(PawnPosition(7, 4))).flatMap { g =>
        gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(1, 4)))
      }
      _ = updatedGame.state.players.activePlayer.id shouldEqual game.state.players.activePlayer.id
      _ = updatedGame.state.players.activePlayer.pawnPosition shouldEqual PawnPosition(7, 4)
      _ = updatedGame.state.players.enemies.head.pawnPosition shouldEqual PawnPosition(1, 4)
    } yield ()
  }

  it should "make place wall move" in {
    for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      _ <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      game <- gameCreator.startGame(protoGame.id, user.id)
      updatedGame <- gameService.makeMove(game.id, game.state.players.activePlayer.id, PlaceWall(WallPosition(Vertical, 1, 4))).flatMap { g =>
        gameService.makeMove(g.id, g.state.players.activePlayer.id, PlaceWall(WallPosition(Vertical, 6, 4)))
      }
      _ = updatedGame.state.players.activePlayer.id shouldEqual game.state.players.activePlayer.id
      _ = updatedGame.state.walls shouldEqual Set(WallPosition(Vertical, 1, 4), WallPosition(Vertical, 6, 4))
    } yield ()
  }

  it should "not move for an external users" in {
    val test = for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      thirdUser <- userService.createUser(thirdUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      _ <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      game <- gameCreator.startGame(protoGame.id, user.id)
      _ <- gameService.makeMove(game.id, thirdUser.id, PlaceWall(WallPosition(Vertical, 1, 4)))
    } yield ()

    test.map(_ => fail).handleError {
      case _: GameInterloperException => succeed
    }
  }

  it should "not move for a non active player" in {
    val test = for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      _ <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      game <- gameCreator.startGame(protoGame.id, user.id)
      _ <- gameService.makeMove(game.id, game.state.players.enemies.head.id, PawnMove(PawnPosition(7, 4)))
    } yield ()

    test.map(_ => fail).handleError {
      case _: WrongPlayersTurnException => succeed
    }
  }

  it should "not move if game is finished" in {
    val test = for {
      user <- userService.createUser(userLogin)
      otherUser <- userService.createUser(otherUserLogin)
      protoGame <- gameCreator.createGame(user.id)
      _ <- gameCreator.joinPlayer(protoGame.id, otherUser.id)
      game <- gameCreator.startGame(protoGame.id, user.id)
      updatedGame <- gameService.makeMove(game.id, game.state.players.activePlayer.id, PawnMove(PawnPosition(7, 4)))
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(1, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(6, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(2, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(5, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(3, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(4, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(5, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(3, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(6, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(2, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(7, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(1, 4))) }
        .flatMap { g => gameService.makeMove(g.id, g.state.players.activePlayer.id, PawnMove(PawnPosition(8, 4))) }
      _ = updatedGame.winner.get shouldEqual otherUser
      _ <- gameService.makeMove(updatedGame.id, updatedGame.state.players.activePlayer.id, PawnMove(PawnPosition(0, 4)))
    } yield ()

    test.map(_ => fail).handleError {
      case _: GameHasFinishedException => succeed
    }
  }
}
