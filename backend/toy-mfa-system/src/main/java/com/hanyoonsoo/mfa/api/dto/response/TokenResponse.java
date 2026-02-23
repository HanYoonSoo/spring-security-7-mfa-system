package com.hanyoonsoo.mfa.api.dto.response;

import org.springframework.http.ResponseCookie;

public record TokenResponse(
        String accessToken,
        ResponseCookie refreshCookie
) {
}
