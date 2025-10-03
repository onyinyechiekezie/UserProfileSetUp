package com.userprofilesetup.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyInUse(EmailAlreadyInUseException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT, "EMAIL_IN_USE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<ApiError> handleInvalidEmail(InvalidEmailFormatException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "INVALID_EMAIL", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(VerificationPendingException.class)
    public ResponseEntity<ApiError> handleVerificationPending(VerificationPendingException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT, "VERIFICATION_PENDING", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
