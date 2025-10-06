package com.userprofilesetup.dtos.responses;

import com.userprofilesetup.data.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String email;
    private String message;
    private String token;
    private UserStatus status;// JWT
}
