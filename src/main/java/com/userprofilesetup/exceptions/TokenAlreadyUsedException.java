package com.userprofilesetup.exceptions;

public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException(String message) {

        super("TOKEN ALREADY USED");
    }
}
