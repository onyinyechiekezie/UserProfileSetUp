package com.userprofilesetup.services;

import com.userprofilesetup.data.models.User;
import com.userprofilesetup.dtos.requests.SignUpRequest;
import com.userprofilesetup.dtos.responses.SignUpResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface UserService {

    User createUser(User user);
    SignUpResponse signup(SignUpRequest request);
    ApiResponse verifyEmail(String token);


}
