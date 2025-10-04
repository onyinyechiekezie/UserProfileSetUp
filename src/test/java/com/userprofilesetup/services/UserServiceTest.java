package com.userprofilesetup.services;

import com.userprofilesetup.data.enums.UserStatus;
import com.userprofilesetup.data.models.User;
import com.userprofilesetup.data.repositories.UserRepository;
import com.userprofilesetup.dtos.requests.SignUpRequest;
import com.userprofilesetup.dtos.responses.SignUpResponse;
import com.userprofilesetup.dtos.responses.VerifyEmailResponse;
import com.userprofilesetup.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    void signupShouldCreateNewUserWithPendingVerification() {
        SignUpRequest request = new SignUpRequest("newuser@example.com", "password123");
        SignUpResponse response = userService.signup(request);

        assertThat(response.getMessage()).isEqualTo("Account created, please verify your email");
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION.name());
        assertThat(response.getVerificationToken()).isNotNull();

        User saved = userRepository.findByEmail("newuser@example.com")
                .orElseThrow(() -> new AssertionError("User not found after signup"));

        assertThat(saved.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(saved.getPassword()).isNotEqualTo("password123");
        assertThat(saved.getPassword()).startsWith("$2a$"); // BCrypt hash
    }

    @Test
    void signupShouldRejectIfEmailAlreadyVerified() {
        User activeUser = new User();
        activeUser.setEmail("active@example.com");
        activeUser.setPassword(passwordEncoder.encode("password123"));
        activeUser.setStatus(UserStatus.VERIFIED);
        userRepository.save(activeUser);

        SignUpRequest request = new SignUpRequest("active@example.com", "newPassword");
        assertThrows(EmailAlreadyInUseException.class, () -> userService.signup(request));
    }

    @Test
    void signupShouldResendVerificationIfNotVerified() {
        User pendingUser = new User();
        pendingUser.setEmail("pending@example.com");
        pendingUser.setPassword(passwordEncoder.encode("password123"));
        pendingUser.setStatus(UserStatus.PENDING_VERIFICATION);
        pendingUser.setVerificationToken("oldToken");
        userRepository.save(pendingUser);

        SignUpRequest request = new SignUpRequest("pending@example.com", "newPassword");
        assertThrows(VerificationPendingException.class, () -> userService.signup(request));

        User updated = userRepository.findByEmail("pending@example.com")
                .orElseThrow(() -> new AssertionError("User not found after re-signup"));

        assertThat(updated.getVerificationToken()).isNotEqualTo("oldToken");
    }

    @Test
    void signupShouldRejectInvalidEmailFormat() {
        SignUpRequest request = new SignUpRequest("not-an-email", "password123");

        assertThrows(InvalidEmailFormatException.class, () -> userService.signup(request));

        Optional<User> result = userRepository.findByEmail("not-an-email");
        assertThat(result).isEmpty();
    }

    @Test
    void verifyEmailShouldActivateUserWithValidToken() {
        User user = new User();
        user.setEmail("verifyme@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setVerificationToken("validToken");
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(30)); // still valid
        userRepository.save(user);

        VerifyEmailResponse response = userService.verifyEmail("validToken");

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("verifyme@example.com");
        assertThat(response.getMessage()).isEqualTo("Email verified successfully. You can now log in.");
        assertThat(response.getStatus()).isEqualTo(UserStatus.VERIFIED);

        User updated = userRepository.findByEmail("verifyme@example.com")
                .orElseThrow(() -> new AssertionError("User not found after verification"));

        assertThat(updated.getStatus()).isEqualTo(UserStatus.VERIFIED);
        assertThat(updated.getVerificationToken()).isNull();
    }

    @Test
    void verifyEmailShouldThrowExceptionForInvalidToken() {
        assertThrows(InvalidTokenException.class, () -> userService.verifyEmail("invalidToken"));
    }

    @Test
    void verifyEmailShouldThrowExceptionForExpiredToken() {
        User user = new User();
        user.setEmail("expired@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setVerificationToken("expiredToken");
        user.setTokenExpiryDate(LocalDateTime.now().minusMinutes(1)); // expired
        userRepository.save(user);

        assertThrows(TokenExpiredException.class, () -> userService.verifyEmail("expiredToken"));
    }

    @Test
    void verifyEmailShouldThrowExceptionIfAlreadyVerified() {
        User user = new User();
        user.setEmail("verified@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setStatus(UserStatus.VERIFIED);
        user.setVerificationToken("usedToken");
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        assertThrows(TokenAlreadyUsedException.class, () -> userService.verifyEmail("usedToken"));
    }
}
