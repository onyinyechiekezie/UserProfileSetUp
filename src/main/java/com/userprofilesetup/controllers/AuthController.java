package com.userprofilesetup.controllers;

import com.userprofilesetup.dtos.requests.SignUpRequest;
import com.userprofilesetup.dtos.responses.SignUpResponse;
import com.userprofilesetup.dtos.responses.VerifyEmailResponse;
import com.userprofilesetup.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for signup, verification, login, logout")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Sign up a new user",
            description = "Creates a user account and sends verification email."
    )
    @ApiResponse(responseCode = "200", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or bad email format")
    @ApiResponse(responseCode = "409", description = "Email already in use or pending verification")
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(userService.signup(request));
    }

    @Operation(
            summary = "Verify email",
            description = "Verifies user email using the token sent via email."
    )
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired verification token")
    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(userService.verifyEmail(token));
    }
}
