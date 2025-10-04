package com.userprofilesetup.exceptions;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {

      super("TOKEN EXPIRED");
    }
}
