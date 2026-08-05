package com.example.prompthubbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendMailRequest(
        @NotBlank(message = "Email người nhận không được để trống")
        @Email(message = "Email người nhận không hợp lệ")
        String to,

        @NotBlank(message = "Tiêu đề không được để trống")
        String subject,

        @NotBlank(message = "Nội dung không được để trống")
        String content
) {
}