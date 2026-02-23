package com.hanyoonsoo.mfa.security.mfa;

import com.hanyoonsoo.mfa.entity.User;
import com.hanyoonsoo.mfa.repository.UserRepository;
import com.hanyoonsoo.mfa.security.jwt.JwtUserClaims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.web.authentication.ott.GenerateOneTimeTokenRequestResolver;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MfaGenerateOneTimeTokenRequestResolver implements GenerateOneTimeTokenRequestResolver {
    private final UserRepository userRepository;

    @Override
    public GenerateOneTimeTokenRequest resolve(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Resolve OTT generate request. authentication={}", authentication);
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("MFA_PASSWORD_FACTOR_REQUIRED");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof JwtUserClaims claims)) {
            throw new RuntimeException("MFA_PASSWORD_FACTOR_REQUIRED");
        }

        User user = userRepository.findById(UUID.fromString(claims.id()))
                .orElseThrow(() -> new RuntimeException("해당 유저는 존재하지 않습니다."));

        request.setAttribute("mfaUserId", user.getId().toString());
        request.setAttribute("mfaEmail", user.getEmail());
        return new GenerateOneTimeTokenRequest(user.getId().toString(), Duration.ofMinutes(5));
    }
}
