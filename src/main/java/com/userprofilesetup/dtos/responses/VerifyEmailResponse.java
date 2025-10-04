package com.userprofilesetup.dtos.responses;

import com.userprofilesetup.data.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailResponse {
    private String email;
    private String message;
    private UserStatus status;
}


