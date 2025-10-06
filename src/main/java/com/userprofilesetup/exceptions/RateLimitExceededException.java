package com.userprofilesetup.exceptions;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {

        super("Too many login attempts. Please try again later.");
    }
}
