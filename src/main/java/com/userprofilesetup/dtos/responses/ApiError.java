package com.userprofilesetup.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ApiError {
    private String error;
    private String message;
    private int status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public static ApiError of(String error, String message, int status) {
        return ApiError.builder()
                .error(error)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
