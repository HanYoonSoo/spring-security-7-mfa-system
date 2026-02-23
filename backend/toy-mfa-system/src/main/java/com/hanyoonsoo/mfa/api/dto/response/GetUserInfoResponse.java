package com.hanyoonsoo.mfa.api.dto.response;

import com.hanyoonsoo.mfa.entity.UserRole;

import java.util.UUID;

public record GetUserInfoResponse(
        UUID id,
        String username,
        String email,
        UserRole role
) {
}
