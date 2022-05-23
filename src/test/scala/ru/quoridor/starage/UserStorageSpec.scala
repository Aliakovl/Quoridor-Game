package ru.quoridor.starage

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import doobie.Transactor
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
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

class UserStorageSpec extends AnyFlatSpec
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

  private val userLogin = "user_" + "UserStorageSpec"
  private val otherUserLogin = "other_user_" + "UserStorageSpec"
  private val unknownUserLogin = "unknown_user_" + "UserStorageSpec"
  private val someUserLogin = "some_user_" + "UserStorageSpec"


  behavior of "insert"

  it should "create a new user" in {
    for {
      user <- userStorage.insert(userLogin)
    } yield user.login shouldEqual userLogin

  }.unsafeRunSync()

  it should "not create the same user" in {
    userStorage.insert(userLogin).map(_ => fail).handleError {
      case _: LoginOccupiedException => succeed
    }
  }.unsafeRunSync()


  behavior of "findByLogin"

  it should "find a user by login" in {
    for {
      user <- userStorage.findByLogin(userLogin)
    } yield user.login shouldEqual userLogin

  }

  it should "not find unknown login" in {
    userStorage.findByLogin(unknownUserLogin).map(_ => fail).handleError {
      case _: LoginNotFoundException => succeed
    }
  }


  behavior of "find"

  it should "find a user by id" in {
    for {
      user <- userStorage.insert(someUserLogin)
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
