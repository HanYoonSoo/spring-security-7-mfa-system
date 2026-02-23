package com.hanyoonsoo.mfa.security.config;

import org.springframework.util.AntPathMatcher;

import java.util.Arrays;

public enum AllowedPath {
    USER_SIGN_UP("/api/v1/users"),
    AUTH_SIGN_IN("/api/v1/auth/sign-in");

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private final String pattern;

    AllowedPath(String pattern) {
        this.pattern = pattern;
    }

    public String pattern() {
        return pattern;
    }

    public static String[] permitAllPatterns() {
        return Arrays.stream(values())
                .map(AllowedPath::pattern)
                .toArray(String[]::new);
    }

    public static boolean matchesPermitAll(String requestUri) {
        return Arrays.stream(values())
                .anyMatch(path -> PATH_MATCHER.match(path.pattern, requestUri));
    }
}
