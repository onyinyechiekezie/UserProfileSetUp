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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final Map<String, List<LocalDateTime>> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final int TIME_WINDOW_MINUTES = 10;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public User createUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public SignUpResponse signup(SignUpRequest request) {
        String email = request.getEmail();
        String rawPassword = request.getPassword();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailFormatException(email);
        }

        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User user = existing.get();

            if (user.getStatus() == UserStatus.VERIFIED) {
                throw new EmailAlreadyInUseException(email);
            }

            if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
                // Generate a new token
                user.setVerificationToken(UUID.randomUUID().toString());
                user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
                userRepository.save(user);

                // Send new verification email
                emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
                throw new VerificationPendingException("Verification link resent to " + email);
            }
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setStatus(UserStatus.PENDING_VERIFICATION);
        newUser.setVerificationToken(UUID.randomUUID().toString());
        newUser.setTokenExpiryDate(LocalDateTime.now().plusHours(24));

        userRepository.save(newUser);

        // Send verification email to the new user
        emailService.sendVerificationEmail(newUser.getEmail(), newUser.getVerificationToken());

        return new SignUpResponse(
                "Account created, please verify your email",
                newUser.getEmail(),
                newUser.getStatus().name(),
                newUser.getVerificationToken()
        );
    }

    @Override
    public VerifyEmailResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token."));

        if (user.getStatus() == UserStatus.VERIFIED) {
            throw new TokenAlreadyUsedException("This verification link has already been used.");
        }

        if (user.getTokenExpiryDate() != null && user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification token has expired.");
        }

        user.setStatus(UserStatus.VERIFIED);
        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        return new VerifyEmailResponse(
                user.getEmail(),
                "Email verified successfully. You can now log in.",
                user.getStatus()
        );
    }

    private boolean isRateLimited(String email) {
        List<LocalDateTime> attempts = loginAttempts.getOrDefault(email, new ArrayList<>());
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(TIME_WINDOW_MINUTES);
        attempts.removeIf(time -> time.isBefore(cutoff));
        loginAttempts.put(email, attempts);
        return attempts.size() >= MAX_ATTEMPTS;
    }

    private void recordFailedAttempt(String email) {
        loginAttempts.computeIfAbsent(email, k -> new ArrayList<>()).add(LocalDateTime.now());
    }

    private void resetFailedAttempts(String email) {
        loginAttempts.remove(email);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();

        if (isRateLimited(email)) {
            throw new RateLimitExceededException("RATE_LIMITED");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordFailedAttempt(email);
            throw new InvalidCredentialsException("INVALID_CREDENTIALS");
        }

        if (user.getStatus() != UserStatus.VERIFIED) {
            resendVerificationLink(email);
            throw new EmailNotVerifiedException("EMAIL_NOT_VERIFIED. A new verification link has been sent.");
        }

        resetFailedAttempts(email);

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                user.getEmail(),
                "Login successful",
                token,
                user.getStatus()
        );
    }

    @Override
    public void resendVerificationLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("USER_NOT_FOUND"));

        if (user.getStatus() == UserStatus.VERIFIED) {
            throw new UserAlreadyVerifiedException("USER_ALREADY_VERIFIED");
        }

        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Send actual email
        emailService.sendVerificationEmail(user.getEmail(), newToken);
    }
}
