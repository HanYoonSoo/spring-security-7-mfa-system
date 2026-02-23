package com.hanyoonsoo.mfa.security.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyoonsoo.mfa.common.ApiResponse;
import com.hanyoonsoo.mfa.security.config.CorsAllowedOriginsProperties;
import com.hanyoonsoo.mfa.security.exception.JwtAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private final CorsAllowedOriginsProperties corsAllowedOriginsProperties;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.error(
                "인증되지 않은 사용자 접근, requestURI: {}, accessToken: {}",
                request.getRequestURI(),
                request.getHeader(HttpHeaders.AUTHORIZATION)
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        CustomCorsHeaderConfigurer.setCorsHeader(request, response, corsAllowedOriginsProperties.getOrigins());

        ApiResponse<Void> body;
        if (authException instanceof JwtAuthenticationException jwtAuthenticationException) {
            String message = jwtAuthenticationException.getMessage() == null ? "인증되지 않은 유저입니다." : jwtAuthenticationException.getMessage();
            String code = jwtAuthenticationException.getMessage() == null ? "INVALID_TOKEN" : jwtAuthenticationException.getMessage();
            body = ApiResponse.fail(code, HttpStatus.UNAUTHORIZED, message);
        } else {
            body = ApiResponse.fail("UNKNOWN_AUTH_ERROR", HttpStatus.UNAUTHORIZED, "인증되지 않은 유저입니다.");
        }

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
