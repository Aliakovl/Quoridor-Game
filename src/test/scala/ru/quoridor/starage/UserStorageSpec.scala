package ru.quoridor.starage

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import doobie.Transactor
import doobie.implicits._
import doobie.util.update.Update
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
import scala.io.Source

class UserStorageSpec extends AnyFlatSpec
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


  behavior of "insert"

  it should "create a new user" in {
    for {
      user <- userStorage.insert("some_user")
    } yield user.login shouldEqual "some_user"
  }.unsafeRunSync()

  it should "not create the same user" in {
    userStorage.insert("some_user").handleError {
      case _: LoginOccupiedException => succeed
    }
  }.unsafeRunSync()


  behavior of "findByLogin"

  it should "find a user by login" in {
    for {
      user <- userStorage.findByLogin("some_user")
    } yield user.login shouldEqual "some_user"
  }

  it should "not find unknown login" in {
    userStorage.findByLogin("unknown_user").handleError {
      case _: LoginNotFoundException => succeed
    }
  }


  behavior of "find"

  it should "find a user by id" in {
    for {
      user <- userStorage.insert("user")
      theSameUser <- userStorage.find(user.id)
    } yield theSameUser shouldEqual user
  }

  it should "not find user by unknown id" in {
    userStorage.find(UUID.randomUUID().typed[User]).handleError {
      case _: UserNotFoundException => succeed
    }
  }


  behavior of "history"

  it should "get empty history" in {
    for {
      user <- userStorage.insert("new_user")
      history <- userStorage.history(user.id)
    } yield history shouldEqual List.empty
  }

}
