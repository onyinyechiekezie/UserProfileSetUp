package com.userprofilesetup.exceptions;

public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException(String email) {
        super("Invalid email format: " + email);
    }
}
