package com.hanyoonsoo.mfa.security.utils;

import com.hanyoonsoo.mfa.security.jwt.JwtUserClaims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {
    private SecurityUtils() {
    }

    public static UUID getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof JwtUserClaims userClaims) {
            return parseUuid(userClaims.id());
        }
        if (principal instanceof String userId) {
            return parseUuid(userId);
        }

        return null;
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            return null;
        }
    }
}
