package model

import utils.Typed.ID

case class User(id: ID[User], login: String)
