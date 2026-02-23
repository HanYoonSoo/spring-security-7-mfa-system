package com.hanyoonsoo.mfa.security.jwt;

import com.hanyoonsoo.mfa.common.Pair;
import com.hanyoonsoo.mfa.entity.UserRole;
import com.hanyoonsoo.mfa.security.exception.JwtAuthenticationException;
import com.hanyoonsoo.mfa.security.mfa.SecurityFactor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtProvider {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "s_rt";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.token.access-token-expiration}")
    private Long defaultAccessTokenExpiration;

    @Value("${jwt.token.refresh-token-expiration}")
    private Long defaultRefreshTokenExpiration;

    @Value("${cookie.same-site}")
    private String cookieSameSite;

    @Value("${cookie.secure}")
    private Boolean cookieSecure;

    private SecretKey secretKey;

    @PostConstruct
    public void initSecretKey() {
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
    }

    public Pair<String, String> createTokens(String userId, List<UserRole> roles) {
        return createTokens(userId, roles, List.of(SecurityFactor.PASSWORD));
    }

    public Pair<String, String> createTokens(String userId, List<UserRole> roles, List<String> factors) {
        Pair<Claims, Claims> claimsPair = getAccessTokenAndRefreshTokenClaims(userId, roles, factors);

        String accessToken = Jwts.builder()
                .setClaims(claimsPair.getLeft())
                .signWith(secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setClaims(claimsPair.getRight())
                .signWith(secretKey)
                .compact();

        return Pair.of(accessToken, refreshToken);
    }

    public JwtUserClaims validateAndExtractUserClaimsFromAccessToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("ACCESS_TOKEN_EXPIRED");
        } catch (Exception e) {
            throw new JwtAuthenticationException("INVALID_TOKEN");
        }

        if (!issuer.equals(claims.getIssuer())) {
            throw new JwtAuthenticationException("INVALID_TOKEN");
        }

        return extractClaims(claims);
    }

    public JwtUserClaims parseUserClaimsFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return extractClaims(claims);
        } catch (ExpiredJwtException e) {
            return extractClaims(e.getClaims());
        }
    }

    public String parseUserIdFromRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("REFRESH_TOKEN_EXPIRED");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new JwtAuthenticationException("INVALID_TOKEN");
        }
    }

    public long getAccessTokenExpirationMillis() {
        return defaultAccessTokenExpiration * 1000L;
    }

    public long getRefreshTokenExpirationMillis() {
        return defaultRefreshTokenExpiration * 1000L;
    }

    public ResponseCookie generateRefreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(defaultRefreshTokenExpiration)
                .sameSite(cookieSameSite)
                .build();
    }

    public String validateRefreshTokenAboutCookies(@Nullable Cookie[] cookies) {
        if (cookies == null) {
            throw new JwtAuthenticationException("No Refresh Token found in cookies");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new JwtAuthenticationException("No Refresh Token found in cookies");
        }

        throwIfRefreshTokenExpired(refreshToken);
        return refreshToken;
    }

    private Pair<Claims, Claims> getAccessTokenAndRefreshTokenClaims(
            String userId,
            List<UserRole> roles,
            List<String> factors
    ) {
        Date now = new Date();

        Claims accessTokenClaims = Jwts.claims();
        accessTokenClaims.setIssuedAt(now);
        accessTokenClaims.setIssuer(issuer);
        accessTokenClaims.setExpiration(new Date(now.getTime() + 1000L * defaultAccessTokenExpiration));
        accessTokenClaims.setSubject(userId);
        accessTokenClaims.put("roles", roles.stream().map(Enum::name).collect(Collectors.toList()));
        accessTokenClaims.put("factors", factors);

        Claims refreshTokenClaims = Jwts.claims();
        refreshTokenClaims.setIssuedAt(now);
        refreshTokenClaims.setIssuer(issuer);
        refreshTokenClaims.setExpiration(new Date(now.getTime() + 1000L * defaultRefreshTokenExpiration));
        refreshTokenClaims.setSubject(userId);

        return Pair.of(accessTokenClaims, refreshTokenClaims);
    }

    private JwtUserClaims extractClaims(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (!(rolesObj instanceof Collection<?> rolesCollection)) {
            throw new JwtAuthenticationException("INVALID_TOKEN");
        }

        List<UserRole> roles = rolesCollection.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> {
                    try {
                        return UserRole.valueOf(role);
                    } catch (IllegalArgumentException e) {
                        throw new JwtAuthenticationException("INVALID_TOKEN");
                    }
                })
                .toList();

        if (roles.size() != rolesCollection.size()) {
            throw new JwtAuthenticationException("INVALID_TOKEN");
        }

        Object factorsObj = claims.get("factors");
        List<String> factors;
        if (factorsObj instanceof Collection<?> factorsCollection) {
            factors = factorsCollection.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
            if (factors.size() != factorsCollection.size()) {
                throw new JwtAuthenticationException("INVALID_TOKEN");
            }
        } else {
            factors = List.of();
        }

        return JwtUserClaims.of(claims.getSubject(), roles, factors);
    }

    private Claims throwIfRefreshTokenExpired(String refreshToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("refresh token expired: {}", e.getMessage());
            throw new JwtAuthenticationException("REFRESH_TOKEN_EXPIRED");
        } catch (Exception e) {
            log.error("invalid refresh token: {}", e.getMessage());
            throw new JwtAuthenticationException("INVALID_TOKEN");
        }
    }
}
