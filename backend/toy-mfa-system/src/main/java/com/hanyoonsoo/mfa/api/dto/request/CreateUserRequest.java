package com.hanyoonsoo.mfa.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank
        String username,
        @NotBlank
        String password,
        @NotBlank
        @Email
        String email
) {}
