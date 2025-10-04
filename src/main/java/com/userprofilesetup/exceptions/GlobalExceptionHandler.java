package com.userprofilesetup.exceptions;

import com.userprofilesetup.dtos.responses.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyInUse(EmailAlreadyInUseException ex) {
        log.warn("Email already in use: {}", ex.getMessage());
        ApiError error = ApiError.of("EMAIL_IN_USE", ex.getMessage(), HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<ApiError> handleInvalidEmail(InvalidEmailFormatException ex) {
        log.warn("Invalid email format: {}", ex.getMessage());
        ApiError error = ApiError.of("INVALID_EMAIL", ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(VerificationPendingException.class)
    public ResponseEntity<ApiError> handleVerificationPending(VerificationPendingException ex) {
        log.info("Verification pending: {}", ex.getMessage());
        ApiError error = ApiError.of("VERIFICATION_PENDING", ex.getMessage(), HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiError> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());
        ApiError error = ApiError.of("TOKEN_INVALID", ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiError> handleTokenExpired(TokenExpiredException ex) {
        log.warn("Expired token: {}", ex.getMessage());
        ApiError error = ApiError.of("TOKEN_EXPIRED", ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<ApiError> handleTokenAlreadyUsed(TokenAlreadyUsedException ex) {
        log.info("Token already used: {}", ex.getMessage());
        ApiError error = ApiError.of("TOKEN_ALREADY_USED", ex.getMessage(), HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ApiError error = ApiError.of(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
