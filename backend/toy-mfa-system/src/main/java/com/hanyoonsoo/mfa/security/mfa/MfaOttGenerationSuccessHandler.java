package com.hanyoonsoo.mfa.security.mfa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyoonsoo.mfa.common.ApiResponse;
import com.hanyoonsoo.mfa.security.config.CorsAllowedOriginsProperties;
import com.hanyoonsoo.mfa.security.custom.CustomCorsHeaderConfigurer;
import com.hanyoonsoo.mfa.service.MfaEmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class MfaOttGenerationSuccessHandler implements OneTimeTokenGenerationSuccessHandler {
    private final MfaEmailService mfaEmailService;
    private final ObjectMapper objectMapper;
    private final CorsAllowedOriginsProperties corsAllowedOriginsProperties;
    @Value("${mfa.magic-link-base-url:http://localhost:3000/mfa/callback.html}")
    private String magicLinkBaseUrl;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken) throws IOException {
        Object emailAttr = request.getAttribute("mfaEmail");
        if (!(emailAttr instanceof String email) || email.isBlank()) {
            throw new RuntimeException("MFA_EMAIL_REQUIRED");
        }

        String encodedToken = URLEncoder.encode(oneTimeToken.getTokenValue(), StandardCharsets.UTF_8);
        String magicLink = magicLinkBaseUrl + "?token=" + encodedToken;
        mfaEmailService.sendMagicLink(email, magicLink);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        CustomCorsHeaderConfigurer.setCorsHeader(request, response, corsAllowedOriginsProperties.getOrigins());
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.success(HttpStatus.OK)));
    }
}
