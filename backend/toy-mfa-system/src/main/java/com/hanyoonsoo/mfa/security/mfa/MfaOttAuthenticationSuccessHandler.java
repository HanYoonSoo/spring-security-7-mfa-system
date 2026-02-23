package com.hanyoonsoo.mfa.security.mfa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyoonsoo.mfa.api.dto.response.TokenResponse;
import com.hanyoonsoo.mfa.common.ApiResponse;
import com.hanyoonsoo.mfa.common.Pair;
import com.hanyoonsoo.mfa.entity.User;
import com.hanyoonsoo.mfa.infra.redis.service.AuthRedisService;
import com.hanyoonsoo.mfa.repository.UserRepository;
import com.hanyoonsoo.mfa.security.config.CorsAllowedOriginsProperties;
import com.hanyoonsoo.mfa.security.custom.CustomCorsHeaderConfigurer;
import com.hanyoonsoo.mfa.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MfaOttAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AuthRedisService authRedisService;
    private final ObjectMapper objectMapper;
    private final CorsAllowedOriginsProperties corsAllowedOriginsProperties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        User user = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("해당 유저는 존재하지 않습니다."));

        Pair<String, String> tokens = jwtProvider.createTokens(
                user.getId().toString(),
                List.of(user.getRole()),
                List.of(SecurityFactor.PASSWORD, SecurityFactor.OTT)
        );

        String accessToken = tokens.getLeft();
        String refreshToken = tokens.getRight();

        authRedisService.saveRefreshToken(
                user.getId().toString(),
                refreshToken,
                jwtProvider.getRefreshTokenExpirationMillis()
        );

        ResponseCookie refreshCookie = jwtProvider.generateRefreshCookie(refreshToken);
        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshCookie);

        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        CustomCorsHeaderConfigurer.setCorsHeader(request, response, corsAllowedOriginsProperties.getOrigins());
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.success(HttpStatus.OK)));
    }
}
