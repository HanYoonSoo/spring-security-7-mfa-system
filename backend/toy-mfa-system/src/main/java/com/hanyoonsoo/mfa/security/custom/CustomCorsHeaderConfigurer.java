package com.hanyoonsoo.mfa.security.custom;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

import java.util.List;

public final class CustomCorsHeaderConfigurer {
    private CustomCorsHeaderConfigurer() {
    }

    public static void setCorsHeader(
            HttpServletRequest request,
            HttpServletResponse response,
            List<String> allowedOrigins
    ) {
        if (request == null || response == null) {
            return;
        }

        String origin = request.getHeader(HttpHeaders.ORIGIN);
        boolean isAllowedOrigin = origin != null
                && allowedOrigins != null
                && allowedOrigins.contains(origin);

        if (!isAllowedOrigin) {
            return;
        }

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type, X-Requested-With");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
    }
}
