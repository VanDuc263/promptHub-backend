package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.request.LoginRequest;
import com.example.prompthubbackend.dto.request.RegisterRequest;
import com.example.prompthubbackend.dto.response.LoginResponse;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.Role;
import com.example.prompthubbackend.model.enums.UserStatus;
import com.example.prompthubbackend.repository.UserRepository;
import com.example.prompthubbackend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void loginReturnsTokenAndUserForValidCredentials() {
        LoginRequest request = request("ducngo", null, "Password@123");
        User user = activeUser();

        when(userRepository.findByUsernameIgnoreCase("ducngo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", user.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken("ducngo", Role.ADMIN)).thenReturn("jwt-token");
        when(jwtUtil.getExpirationSeconds()).thenReturn(86400L);

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(Role.ADMIN, response.user().role());
        assertNotNull(user.getLastLoginAt());
        verify(userRepository).save(user);
    }

    @Test
    void loginRejectsInvalidPasswordWithoutRevealingUserDetails() {
        LoginRequest request = request(null, "duc@example.com", "wrong-password");
        User user = activeUser();

        when(userRepository.findByEmailIgnoreCase("duc@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPasswordHash()))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Invalid username/email or password", exception.getReason());
    }

    @Test
    void registerCreatesActiveUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest(
                "New User", "new_user", "NEW@example.com", "Password1"
        );

        when(userRepository.existsByUsernameIgnoreCase("new_user")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(jwtUtil.generateToken("new_user", Role.USER)).thenReturn("new-token");
        when(jwtUtil.getExpirationSeconds()).thenReturn(86400L);

        LoginResponse response = authService.register(request);

        assertEquals("new-token", response.accessToken());
        assertEquals("new@example.com", response.user().email());
        assertEquals("New User", response.user().displayName());
        assertEquals(Role.USER, response.user().role());
    }

    @Test
    void usernameAvailabilityUsesCaseInsensitiveLookup() {
        when(userRepository.existsByUsernameIgnoreCase("new_user")).thenReturn(false);

        assertEquals(true, authService.isUsernameAvailable("new_user"));
        verify(userRepository).existsByUsernameIgnoreCase("new_user");
    }

    private LoginRequest request(String username, String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private User activeUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("ducngo")
                .email("duc@example.com")
                .passwordHash("$2a$10$encoded")
                .displayName("Duc Ngo")
                .status(UserStatus.ACTIVE)
                .isSystemAdmin(true)
                .build();
    }
}
