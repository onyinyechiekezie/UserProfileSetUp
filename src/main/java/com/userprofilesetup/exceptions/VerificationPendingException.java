package com.userprofilesetup.exceptions;

public class VerificationPendingException extends RuntimeException {
    public VerificationPendingException(String email) {
        super("Verification already pending for: " + email);
    }
}
