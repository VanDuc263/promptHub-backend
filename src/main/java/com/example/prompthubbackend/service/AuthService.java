package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.request.LoginRequest;
import com.example.prompthubbackend.dto.request.RegisterRequest;
import com.example.prompthubbackend.dto.response.LoginResponse;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.Role;
import com.example.prompthubbackend.model.enums.UserStatus;
import com.example.prompthubbackend.repository.UserRepository;
import com.example.prompthubbackend.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = findUser(request)
                .orElseThrow(this::invalidCredentials);

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        if (user.getStatus() != UserStatus.ACTIVE || user.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        return createLoginResponse(user);
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        OffsetDateTime now = OffsetDateTime.now();
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.fullName().trim())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .isSystemAdmin(false)
                .createdAt(now)
                .updatedAt(now)
                .lastLoginAt(now)
                .build();

        return createLoginResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        if (username == null || !username.matches("^[A-Za-z0-9_-]{3,30}$")) {
            return false;
        }
        return !userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    private LoginResponse createLoginResponse(User user) {
        Role role = Boolean.TRUE.equals(user.getIsSystemAdmin()) ? Role.ADMIN : Role.USER;
        String accessToken = jwtUtil.generateToken(user.getUsername(), role);
        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtUtil.getExpirationSeconds(),
                new LoginResponse.UserResponse(
                        user.getId(), user.getUsername(), user.getEmail(), user.getDisplayName(),
                        user.getAvatarUrl(), role
                )
        );
    }

    private Optional<User> findUser(LoginRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return userRepository.findByEmailIgnoreCase(request.getEmail().trim());
        }
        return userRepository.findByUsernameIgnoreCase(request.getUsername().trim());
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username/email or password"
        );
    }
}
