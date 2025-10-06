package com.userprofilesetup.services;

import com.userprofilesetup.data.enums.UserStatus;
import com.userprofilesetup.data.models.User;
import com.userprofilesetup.data.repositories.UserRepository;
import com.userprofilesetup.dtos.requests.LoginRequest;
import com.userprofilesetup.dtos.requests.SignUpRequest;
import com.userprofilesetup.dtos.responses.LoginResponse;
import com.userprofilesetup.dtos.responses.SignUpResponse;
import com.userprofilesetup.dtos.responses.VerifyEmailResponse;
import com.userprofilesetup.exceptions.*;
import com.userprofilesetup.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    //SIGNUP TESTS
    @Test
    void signupShouldCreateNewUserAndSendVerificationEmail() {
        SignUpRequest request = new SignUpRequest("newuser@example.com", "password123");

        SignUpResponse response = userService.signup(request);

        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION.name());
        assertThat(response.getMessage()).contains("Account created");
        assertThat(response.getVerificationToken()).isNotNull();

        verify(emailService, times(1))
                .sendVerificationEmail(eq("newuser@example.com"), anyString());
    }

    @Test
    void signupShouldRejectIfEmailAlreadyVerified() {
        User verified = new User();
        verified.setEmail("verified@example.com");
        verified.setPassword(passwordEncoder.encode("12345"));
        verified.setStatus(UserStatus.VERIFIED);
        userRepository.save(verified);

        SignUpRequest request = new SignUpRequest("verified@example.com", "newPassword");

        assertThrows(EmailAlreadyInUseException.class, () -> userService.signup(request));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void signupShouldResendIfPendingVerification() {
        User pending = new User();
        pending.setEmail("pending@example.com");
        pending.setPassword(passwordEncoder.encode("12345"));
        pending.setStatus(UserStatus.PENDING_VERIFICATION);
        pending.setVerificationToken("oldToken");
        userRepository.save(pending);

        SignUpRequest request = new SignUpRequest("pending@example.com", "newPassword");

        assertThrows(VerificationPendingException.class, () -> userService.signup(request));
        verify(emailService, times(1))
                .sendVerificationEmail(eq("pending@example.com"), anyString());
    }

    @Test
    void signupShouldRejectInvalidEmailFormat() {
        SignUpRequest request = new SignUpRequest("bad-email", "password123");
        assertThrows(InvalidEmailFormatException.class, () -> userService.signup(request));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    //VERIFY EMAIL TESTS
    @Test
    void verifyEmailShouldMarkUserAsVerified() {
        User user = new User();
        user.setEmail("verifyme@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setVerificationToken("validToken");
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        VerifyEmailResponse response = userService.verifyEmail("validToken");

        assertThat(response.getEmail()).isEqualTo("verifyme@example.com");
        assertThat(response.getStatus()).isEqualTo(UserStatus.VERIFIED);
        assertThat(response.getMessage()).contains("Email verified successfully");

        User updated = userRepository.findByEmail("verifyme@example.com").get();
        assertThat(updated.getStatus()).isEqualTo(UserStatus.VERIFIED);
        assertThat(updated.getVerificationToken()).isNull();
    }

    @Test
    void verifyEmailShouldThrowIfInvalidToken() {
        assertThrows(InvalidTokenException.class, () -> userService.verifyEmail("badToken"));
    }

    @Test
    void verifyEmailShouldThrowIfExpiredToken() {
        User user = new User();
        user.setEmail("expired@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setVerificationToken("expired");
        user.setTokenExpiryDate(LocalDateTime.now().minusMinutes(1));
        userRepository.save(user);

        assertThrows(TokenExpiredException.class, () -> userService.verifyEmail("expired"));
    }

    @Test
    void verifyEmailShouldThrowIfAlreadyVerified() {
        User user = new User();
        user.setEmail("already@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setStatus(UserStatus.VERIFIED);
        user.setVerificationToken("used");
        userRepository.save(user);

        assertThrows(TokenAlreadyUsedException.class, () -> userService.verifyEmail("used"));
    }

    //LOGIN TESTS
    @Test
    void loginShouldReturnTokenForVerifiedUser() {
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        user.setStatus(UserStatus.VERIFIED);
        userRepository.save(user);

        when(jwtService.generateToken("login@example.com")).thenReturn("mocked-token");

        LoginRequest request = new LoginRequest("login@example.com", "secret");
        LoginResponse response = userService.login(request);

        assertThat(response.getToken()).isEqualTo("mocked-token");
        assertThat(response.getMessage()).isEqualTo("Login successful");
        assertThat(response.getStatus()).isEqualTo(UserStatus.VERIFIED);

        verify(jwtService, times(1)).generateToken("login@example.com");
    }

    @Test
    void loginShouldThrowIfEmailNotVerifiedAndResendVerification() {
        User user = new User();
        user.setEmail("unverified@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("unverified@example.com", "secret");

        assertThrows(EmailNotVerifiedException.class, () -> userService.login(request));
        verify(emailService, times(1))
                .sendVerificationEmail(eq("unverified@example.com"), anyString());
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        User user = new User();
        user.setEmail("wrongpass@example.com");
        user.setPassword(passwordEncoder.encode("correct"));
        user.setStatus(UserStatus.VERIFIED);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("wrongpass@example.com", "wrong");
        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
        verify(jwtService, never()).generateToken(anyString());
    }

    //RESEND VERIFICATION LINK TEST

    @Test
    void resendVerificationShouldSendEmailIfPending() {
        User user = new User();
        user.setEmail("resend@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setVerificationToken("old");
        userRepository.save(user);

        userService.resendVerificationLink("resend@example.com");

        verify(emailService, times(1))
                .sendVerificationEmail(eq("resend@example.com"), anyString());

        User updated = userRepository.findByEmail("resend@example.com").get();
        assertThat(updated.getVerificationToken()).isNotEqualTo("old");
    }

    @Test
    void resendVerificationShouldThrowIfUserNotFound() {
        assertThrows(InvalidCredentialsException.class,
                () -> userService.resendVerificationLink("noexist@example.com"));
    }

    @Test
    void resendVerificationShouldThrowIfAlreadyVerified() {
        User verified = new User();
        verified.setEmail("verifieduser@example.com");
        verified.setPassword(passwordEncoder.encode("12345"));
        verified.setStatus(UserStatus.VERIFIED);
        userRepository.save(verified);

        assertThrows(UserAlreadyVerifiedException.class,
                () -> userService.resendVerificationLink("verifieduser@example.com"));
    }
}
