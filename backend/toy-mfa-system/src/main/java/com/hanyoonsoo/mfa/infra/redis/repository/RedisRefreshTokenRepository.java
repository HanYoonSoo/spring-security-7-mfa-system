package com.hanyoonsoo.mfa.infra.redis.repository;

import com.hanyoonsoo.mfa.exception.AuthenticationException;
import com.hanyoonsoo.mfa.security.exception.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String key, String refreshToken, long expireMillis) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, refreshToken, Duration.ofMillis(expireMillis));
    }

    public void matchRefreshTokenOrThrow(String key, String refreshToken) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String refreshTokenInDb = valueOperations.get(key);
        if (refreshTokenInDb == null || refreshTokenInDb.isEmpty()) {
            throw new AuthenticationException("REFRESH_TOKEN_EXPIRED");
        }
        if (!refreshTokenInDb.equals(refreshToken)) {
            throw new AuthenticationException("REFRESH_TOKEN_MISMATCH");
        }
    }

    public void deleteRefreshToken(String key) {
        redisTemplate.delete(key);
    }

    public void saveAccessTokenForLogout(String accessToken, long expireMillis) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(accessToken, "logout", Duration.ofMillis(expireMillis));
    }

    public boolean isLogoutAccessToken(String accessToken) {
        return "logout".equals(redisTemplate.opsForValue().get(accessToken));
    }
}
