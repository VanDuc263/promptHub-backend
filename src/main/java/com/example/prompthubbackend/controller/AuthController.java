package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.request.LoginRequest;
import com.example.prompthubbackend.dto.request.RegisterRequest;
import com.example.prompthubbackend.dto.response.LoginResponse;
import com.example.prompthubbackend.dto.response.UsernameAvailabilityResponse;
import com.example.prompthubbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController{
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @GetMapping("/username-available")
    public ResponseEntity<UsernameAvailabilityResponse> usernameAvailable(
            @RequestParam String username
    ) {
        return ResponseEntity.ok(new UsernameAvailabilityResponse(authService.isUsernameAvailable(username)));
    }
}
