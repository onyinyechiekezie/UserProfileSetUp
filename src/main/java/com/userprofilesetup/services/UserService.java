package com.userprofilesetup.services;

import com.userprofilesetup.data.models.User;
import com.userprofilesetup.dtos.requests.LoginRequest;
import com.userprofilesetup.dtos.requests.SignUpRequest;
import com.userprofilesetup.dtos.responses.LoginResponse;
import com.userprofilesetup.dtos.responses.SignUpResponse;
import com.userprofilesetup.dtos.responses.VerifyEmailResponse;


public interface UserService {

    User createUser(User user);
    SignUpResponse signup(SignUpRequest request);
    VerifyEmailResponse verifyEmail(String token);
    LoginResponse login(LoginRequest request);
    void resendVerificationLink(String email);


}
