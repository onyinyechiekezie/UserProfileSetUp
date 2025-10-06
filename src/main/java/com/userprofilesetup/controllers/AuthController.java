package com.userprofilesetup.controllers;

import com.userprofilesetup.dtos.requests.LoginRequest;
import com.userprofilesetup.dtos.requests.SignUpRequest;
import com.userprofilesetup.dtos.responses.LoginResponse;
import com.userprofilesetup.dtos.responses.SignUpResponse;
import com.userprofilesetup.dtos.responses.VerifyEmailResponse;
import com.userprofilesetup.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for signup, verification, login, and verification resend")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    //SIGN UP
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

    //VERIFY EMAIL (when user clicks the link)
    @Operation(
            summary = "Verify email",
            description = "Verifies user email using the token sent via email."
    )
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired verification token")
    @GetMapping("/verify")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(userService.verifyEmail(token));
    }

    //LOGIN
    @Operation(
            summary = "Login user",
            description = "Authenticates a user and returns a JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials or unverified email")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    //RESEND VERIFICATION LINK
    @Operation(
            summary = "Resend verification email",
            description = "Sends a new verification link to the user if not yet verified."
    )
    @ApiResponse(responseCode = "200", description = "Verification email re-sent successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "User already verified")
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestParam String email) {
        userService.resendVerificationLink(email);
        return ResponseEntity.ok("Verification email sent successfully.");
    }
}
