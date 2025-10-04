package com.userprofilesetup.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {

        super("TOKEN INVALID");
    }
}
