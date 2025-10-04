package com.userprofilesetup.exceptions;

public class InvalidOrExpiredTokenException extends RuntimeException {
  public InvalidOrExpiredTokenException(String token) {
    super("Invalid or expired token: " + token);
  }
}
