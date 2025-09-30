package com.userprofilesetup.services;

import com.userprofilesetup.data.enums.UserStatus;
import com.userprofilesetup.data.models.User;
import com.userprofilesetup.data.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(User user) {
        // Ensure password is hashed before saving (if plain provided)
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public String signup(String email, String password) {
        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getStatus() == UserStatus.ACTIVE) {
                // email already verified / active
                return "Email in use";
            } else {
                // pending verification -> resend
                user.setVerificationToken(UUID.randomUUID().toString());
                userRepository.save(user);
                return "Verification resent";
            }
        }
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setStatus(UserStatus.PENDING_VERIFICATION);
        newUser.setVerificationToken(UUID.randomUUID().toString());

        userRepository.save(newUser);
        return "Account created, please verify your email";
    }

}
