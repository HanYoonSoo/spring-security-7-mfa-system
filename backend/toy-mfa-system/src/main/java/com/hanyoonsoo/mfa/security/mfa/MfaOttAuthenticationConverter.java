package com.hanyoonsoo.mfa.security.mfa;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class MfaOttAuthenticationConverter implements AuthenticationConverter {
    @Override
    public Authentication convert(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (token == null || token.isBlank()) {
            return null;
        }
        return new OneTimeTokenAuthenticationToken(token);
    }
}
