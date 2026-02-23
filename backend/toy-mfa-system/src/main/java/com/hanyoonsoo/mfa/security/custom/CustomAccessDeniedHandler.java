package com.hanyoonsoo.mfa.security.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyoonsoo.mfa.common.ApiResponse;
import com.hanyoonsoo.mfa.security.config.CorsAllowedOriginsProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    private final CorsAllowedOriginsProperties corsAllowedOriginsProperties;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        log.error(
                "권한이 없는 사용자의 접근, requestURI: {}, accessToken: {}",
                request.getRequestURI(),
                request.getHeader(HttpHeaders.AUTHORIZATION)
        );

        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        CustomCorsHeaderConfigurer.setCorsHeader(request, response, corsAllowedOriginsProperties.getOrigins());

        ApiResponse<Void> body = ApiResponse.fail(
                "FORBIDDEN_USER",
                HttpStatus.FORBIDDEN,
                "접근 권한이 없습니다."
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
