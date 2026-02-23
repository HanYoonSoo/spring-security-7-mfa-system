package com.hanyoonsoo.mfa.security.mfa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyoonsoo.mfa.common.ApiResponse;
import com.hanyoonsoo.mfa.security.config.CorsAllowedOriginsProperties;
import com.hanyoonsoo.mfa.security.custom.CustomCorsHeaderConfigurer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MfaOttAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;
    private final CorsAllowedOriginsProperties corsAllowedOriginsProperties;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        log.warn(
                "OTT authentication failed. requestURI={}, reason={}",
                request.getRequestURI(),
                exception.getMessage()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        CustomCorsHeaderConfigurer.setCorsHeader(request, response, corsAllowedOriginsProperties.getOrigins());
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.fail("MFA_OTT_AUTH_FAILED", HttpStatus.UNAUTHORIZED, exception.getMessage())
        ));
    }
}
