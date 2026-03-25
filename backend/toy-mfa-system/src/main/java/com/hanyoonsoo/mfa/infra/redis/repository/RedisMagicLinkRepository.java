package com.hanyoonsoo.mfa.infra.redis.repository;

import com.hanyoonsoo.mfa.common.Sha256HashUtils;
import com.hanyoonsoo.mfa.infra.utils.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisMagicLinkRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveMagicLinkLimit(String email) {
        String key = RedisKeys.magicLinkLimit(email);
        redisTemplate.opsForValue().set(key, UUID.randomUUID().toString(), Duration.ofMinutes(1));
    }

    public boolean isMagicLinkLimitExists(String email) {
        String key = RedisKeys.magicLinkLimit(email);
        return redisTemplate.hasKey(key);
    }
}
