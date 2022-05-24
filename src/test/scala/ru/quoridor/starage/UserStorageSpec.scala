package ru.quoridor.starage

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import doobie.Transactor
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig._
import pureconfig.generic.auto._
import ru.quoridor.GameException._
import ru.quoridor.User
import ru.quoridor.app.AppConfig
import ru.quoridor.storage.UserStorage
import ru.quoridor.storage.sqlStorage.UserStorageImpl
import ru.utils.Typed.Implicits.TypedOps

import java.util.UUID

class UserStorageSpec extends AsyncFlatSpec
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

  private def userLogin = s"user_${UUID.randomUUID()}"
  private def otherUserLogin = s"other_user_${UUID.randomUUID()}"
  private def unknownUserLogin = s"unknown_user_${UUID.randomUUID()}"


  behavior of "insert"

  it should "create a new user" in {
    val login = userLogin
    for {
      user <- userStorage.insert(login)
    } yield user.login shouldEqual login
  }

  it should "not create the same user" in {
    val login = userLogin
    val test = for {
      _ <- userStorage.insert(login)
      _ <- userStorage.insert(login)
    } yield ()

    test.map(_ => fail).handleError {
      case _: LoginOccupiedException => succeed
    }
  }


  behavior of "findByLogin"

  it should "find a user by login" in {
    val login = userLogin
    for {
      user <- userStorage.insert(login)
      theSameUser <- userStorage.findByLogin(login)
    } yield theSameUser shouldEqual user
  }

  it should "not find unknown login" in {
    userStorage.findByLogin(unknownUserLogin).map(_ => fail).handleError {
      case _: LoginNotFoundException => succeed
    }
  }


  behavior of "find"

  it should "find a user by id" in {
    for {
      user <- userStorage.insert(userLogin)
      theSameUser <- userStorage.find(user.id)
    } yield theSameUser shouldEqual user
  }

  it should "not find user by unknown id" in {
    userStorage.find(UUID.randomUUID().typed[User]).map(_ => fail).handleError {
      case _: UserNotFoundException => succeed
    }
  }


  behavior of "history"

  it should "get empty history" in {
    for {
      user <- userStorage.insert(otherUserLogin)
      history <- userStorage.history(user.id)
    } yield history shouldEqual List.empty
  }
}
