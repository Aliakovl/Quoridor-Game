package ru.quoridor.auth.model

import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class Credentials(username: Username, password: Password) derives Schema
