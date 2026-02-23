package com.hanyoonsoo.mfa.security.filter;

import com.hanyoonsoo.mfa.infra.redis.service.AuthRedisService;
import com.hanyoonsoo.mfa.repository.UserRepository;
import com.hanyoonsoo.mfa.security.config.AllowedPath;
import com.hanyoonsoo.mfa.security.custom.CustomAuthenticationEntryPoint;
import com.hanyoonsoo.mfa.security.exception.JwtAuthenticationException;
import com.hanyoonsoo.mfa.security.jwt.JwtProvider;
import com.hanyoonsoo.mfa.security.jwt.JwtUserClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final AuthRedisService authRedisService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeaderValue = request.getHeader("Authorization");
        String accessToken = parseAccessToken(authorizationHeaderValue);

        if (authorizationHeaderValue == null || authorizationHeaderValue.isBlank()) {
            SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        } else if (!accessToken.isEmpty()) {
            try {
                throwIfLogoutAccessToken(accessToken);

                JwtUserClaims claims = jwtProvider.validateAndExtractUserClaimsFromAccessToken(accessToken);

                if (!userRepository.existsById(UUID.fromString(claims.id())))
                    throw new JwtAuthenticationException("유저를 찾을 수 없습니다.");

                List<SimpleGrantedAuthority> authorities = Stream.concat(
                                claims.roles().stream().map(role -> new SimpleGrantedAuthority(role.toSpringRole())),
                                claims.factors().stream().map(SimpleGrantedAuthority::new)
                        )
                        .toList();

                SecurityContextHolder.getContext().setAuthentication(
                        new PreAuthenticatedAuthenticationToken(claims, accessToken, authorities)
                );
            } catch (JwtAuthenticationException e) {
                SecurityContextHolder.clearContext();
                customAuthenticationEntryPoint.commence(request, response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getMethod().equals("OPTIONS")
                || AllowedPath.matchesPermitAll(request.getRequestURI());
    }

    private String parseAccessToken(String authorizationHeaderValue) {
        if (authorizationHeaderValue == null) {
            return "";
        }
        return authorizationHeaderValue.replaceAll("Bearer ", "");
    }

    private void throwIfLogoutAccessToken(String accessToken) {
        if (authRedisService.isLogoutAccessToken(accessToken)) {
            throw new JwtAuthenticationException("로그아웃 토큰");
        }
    }
}
