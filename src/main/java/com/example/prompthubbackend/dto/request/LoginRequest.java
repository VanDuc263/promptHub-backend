package com.example.prompthubbackend.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @AssertTrue(message = "Username or email is required")
    public boolean isIdentityProvided() {
        return (username != null && !username.isBlank())
                || (email != null && !email.isBlank());
    }
}
