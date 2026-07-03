package com.aakash.goalkeeper.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100, message = "Password must be at least 8 characters") String password,
            @NotBlank @Size(max = 120) String displayName
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record UserDto(
            UUID id,
            String email,
            String displayName,
            String role,
            boolean emailVerified
    ) {}

    /** Access token in the body; refresh token is set as an httpOnly cookie. */
    public record AuthResponse(
            String accessToken,
            long expiresInSeconds,
            UserDto user
    ) {}
}
