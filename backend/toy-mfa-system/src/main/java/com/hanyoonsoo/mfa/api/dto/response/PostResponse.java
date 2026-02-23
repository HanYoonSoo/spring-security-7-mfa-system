package com.hanyoonsoo.mfa.api.dto.response;

import java.util.UUID;

public record PostResponse(
        Long id,
        String title,
        String content,
        UUID userId
) {
}
