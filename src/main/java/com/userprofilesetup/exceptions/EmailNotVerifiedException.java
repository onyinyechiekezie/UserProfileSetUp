package com.userprofilesetup.exceptions;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {

        super("Email not verified");
    }
}
