package com.hanyoonsoo.mfa.security.jwt;

import com.hanyoonsoo.mfa.entity.UserRole;
import java.util.List;

public record JwtUserClaims(String id, List<UserRole> roles, List<String> factors) {
    public static JwtUserClaims of(String id, List<UserRole> roles, List<String> factors) {
        return new JwtUserClaims(id, roles, factors);
    }

    public static JwtUserClaims of(String id) {
        return new JwtUserClaims(id, List.of(), List.of());
    }
}
