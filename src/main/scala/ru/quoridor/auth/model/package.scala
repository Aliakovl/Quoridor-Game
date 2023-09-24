package ru.quoridor.auth

import ru.quoridor.auth.model.AuthException.*

package object model {
  type InvalidPassword = InvalidPassword.type
  type InvalidAccessToken = InvalidAccessToken.type
  type InvalidRefreshToken = InvalidRefreshToken.type
}
