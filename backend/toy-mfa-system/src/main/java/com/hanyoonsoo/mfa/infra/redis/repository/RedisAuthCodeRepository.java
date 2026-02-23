package com.hanyoonsoo.mfa.infra.redis.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisAuthCodeRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void saveAuthCode(String key, String authCode, long expireMillis) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, authCode, Duration.ofMillis(expireMillis));
    }

    public String findAuthCode(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void saveAuthCodeVerified(String key, String authCodeVerified, long expireMillis) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, authCodeVerified, Duration.ofMillis(expireMillis));
    }
}
