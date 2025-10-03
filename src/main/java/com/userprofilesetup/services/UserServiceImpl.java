package com.userprofilesetup.services;

import com.userprofilesetup.data.enums.UserStatus;
import com.userprofilesetup.data.models.User;
import com.userprofilesetup.data.repositories.UserRepository;
import com.userprofilesetup.dtos.requests.SignUpRequest;
import com.userprofilesetup.dtos.responses.SignUpResponse;
import com.userprofilesetup.exceptions.EmailAlreadyInUseException;
import com.userprofilesetup.exceptions.InvalidEmailFormatException;
import com.userprofilesetup.exceptions.VerificationPendingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

        // Validate email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailFormatException(email);
        }

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getStatus() == UserStatus.ACTIVE) {
                throw new EmailAlreadyInUseException(email);
            }
            if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
                user.setVerificationToken(UUID.randomUUID().toString());
                userRepository.save(user);
                throw new VerificationPendingException(email);
            }
        }

        // New user flow
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setStatus(UserStatus.PENDING_VERIFICATION);
        newUser.setVerificationToken(UUID.randomUUID().toString());
        userRepository.save(newUser);

        // Return structured response DTO
        return new SignUpResponse(
                "Account created, please verify your email",
                newUser.getEmail(),
                newUser.getStatus().name(),
                newUser.getVerificationToken()
        );
    }
}
