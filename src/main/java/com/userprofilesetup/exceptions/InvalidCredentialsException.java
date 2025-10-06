package com.userprofilesetup.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {

        super("Invalid credentials");
    }
}
