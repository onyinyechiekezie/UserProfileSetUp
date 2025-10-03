package com.userprofilesetup.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApiError {
    private String error;
    private String message;
    private int status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public ApiError(HttpStatus status, String error, String message) {
        this.status = status.value();
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
