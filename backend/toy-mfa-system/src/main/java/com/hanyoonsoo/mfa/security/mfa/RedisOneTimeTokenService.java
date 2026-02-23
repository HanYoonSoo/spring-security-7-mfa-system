package com.hanyoonsoo.mfa.security.mfa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@NullMarked
@Slf4j
public class RedisOneTimeTokenService implements OneTimeTokenService {
    private static final String TOKEN_PREFIX = "OTT:TOKEN:";
    private static final String ATTEMPT_PREFIX = "OTT:ATTEMPTS:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration ATTEMPT_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final Clock clock = Clock.systemUTC();

    @Override
    public OneTimeToken generate(GenerateOneTimeTokenRequest request) {
        Instant now = clock.instant();
        Duration expiresIn = request.getExpiresIn();
        Instant expiresAt = now.plus(expiresIn);

        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        String tokenHash = sha256(tokenValue);
        String value = request.getUsername() + "|" + expiresAt.toEpochMilli();

        redisTemplate.opsForValue().set(tokenKey(tokenHash), value, expiresIn);
        log.info("OTT generated. username={}, expiresAt={}", request.getUsername(), expiresAt);
        return new DefaultOneTimeToken(tokenValue, request.getUsername(), expiresAt);
    }

    @Override
    @Nullable
    public OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken) {
        String tokenValue = authenticationToken.getTokenValue();
        if (tokenValue == null || tokenValue.isBlank()) {
            log.warn("OTT consume failed: token is empty");
            return null;
        }

        String tokenHash = sha256(tokenValue);
        if (readAttempts(tokenHash) >= MAX_ATTEMPTS) {
            log.warn("OTT consume failed: too many attempts. tokenHash={}", tokenHash);
            return null;
        }

        String key = tokenKey(tokenHash);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isBlank()) {
            log.warn("OTT consume failed: token not found or already consumed. tokenHash={}", tokenHash);
            return fail(tokenHash);
        }

        String[] parts = value.split("\\|", 2);
        if (parts.length != 2) {
            log.warn("OTT consume failed: malformed token payload in redis. tokenHash={}", tokenHash);
            return fail(tokenHash);
        }

        String storedUserId = parts[0];
        Instant expiresAt;
        try {
            expiresAt = Instant.ofEpochMilli(Long.parseLong(parts[1]));
        } catch (NumberFormatException e) {
            log.warn("OTT consume failed: invalid expiresAt format. tokenHash={}", tokenHash);
            return fail(tokenHash);
        }

        if (expiresAt.isBefore(clock.instant())) {
            log.warn("OTT consume failed: token expired. tokenHash={}, expiresAt={}", tokenHash, expiresAt);
            return failAndDelete(tokenHash, key);
        }

        redisTemplate.delete(key);
        redisTemplate.delete(attemptKey(tokenHash));
        log.info("OTT consume success. username={}", storedUserId);
        return new DefaultOneTimeToken(tokenValue, storedUserId, expiresAt);
    }

    @Nullable
    private OneTimeToken fail(String subjectKey) {
        increaseAttempts(subjectKey, ATTEMPT_TTL);
        return null;
    }

    @Nullable
    private OneTimeToken failAndDelete(String subjectKey, String tokenKey) {
        redisTemplate.delete(tokenKey);
        return fail(subjectKey);
    }

    private void increaseAttempts(String subjectKey, Duration ttl) {
        Long attempts = redisTemplate.opsForValue().increment(attemptKey(subjectKey));
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(attemptKey(subjectKey), ttl);
        }
    }

    private int readAttempts(String subjectKey) {
        String count = redisTemplate.opsForValue().get(attemptKey(subjectKey));
        if (count == null || count.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String tokenKey(String tokenHash) {
        return TOKEN_PREFIX + tokenHash;
    }

    private String attemptKey(String subjectKey) {
        return ATTEMPT_PREFIX + subjectKey;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
