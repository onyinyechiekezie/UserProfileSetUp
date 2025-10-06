package com.userprofilesetup.exceptions;

public class UserAlreadyVerifiedException extends RuntimeException {
    public UserAlreadyVerifiedException(String message) {

        super(message);
    }
}
