package ru.quoridor.auth.model

sealed abstract class AuthException(message: String) extends Exception(message)

object AuthException:
  case object InvalidAccessToken extends AuthException("Invalid access token")

  case object InvalidPassword extends AuthException("Invalid password")

  case object InvalidRefreshToken extends AuthException("Invalid refresh token")
