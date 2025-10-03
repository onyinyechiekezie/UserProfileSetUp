package com.userprofilesetup.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignUpResponse {
    private String message;
    private String email;
    private String status;
    private String verificationToken;
}