package com.hanyoonsoo.mfa.security.mfa;

import com.hanyoonsoo.mfa.common.Sha256HashUtils;
import com.hanyoonsoo.mfa.infra.utils.RedisKeyFactory;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
@NullMarked
@Slf4j
public class RedisOneTimeTokenService implements OneTimeTokenService {
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
        String tokenHash = Sha256HashUtils.hash(tokenValue);
        String value = request.getUsername() + "|" + expiresAt.toEpochMilli();

        redisTemplate.opsForValue().set(RedisKeyFactory.ottToken(tokenHash), value, expiresIn);
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

        String tokenHash = Sha256HashUtils.hash(tokenValue);
        if (isAttemptExceeded(tokenHash)) {
            log.warn("OTT consume failed: too many attempts. tokenHash={}", tokenHash);
            return null;
        }

        TokenLookupResult lookupResult = lookupToken(tokenHash);
        if (!lookupResult.success()) {
            handleFailure(tokenHash, lookupResult);
            return null;
        }

        redisTemplate.delete(RedisKeyFactory.ottToken(tokenHash));
        redisTemplate.delete(RedisKeyFactory.ottAttempt(tokenHash));
        log.info("OTT consume success. username={}", lookupResult.userId());
        return new DefaultOneTimeToken(tokenValue, lookupResult.userId(), lookupResult.expiresAt());
    }

    private boolean isAttemptExceeded(String tokenHash) {
        return readAttempts(tokenHash) >= MAX_ATTEMPTS;
    }

    private TokenLookupResult lookupToken(String tokenHash) {
        String value = redisTemplate.opsForValue().get(RedisKeyFactory.ottToken(tokenHash));
        if (value == null || value.isBlank()) {
            return TokenLookupResult.notFound();
        }

        String[] parts = value.split("\\|", 2);
        if (parts.length != 2) {
            return TokenLookupResult.malformed();
        }

        try {
            Instant expiresAt = Instant.ofEpochMilli(Long.parseLong(parts[1]));
            return TokenLookupResult.success(parts[0], expiresAt);
        } catch (NumberFormatException e) {
            return TokenLookupResult.malformed();
        }
    }

    private void handleFailure(String tokenHash, TokenLookupResult lookupResult) {
        switch (lookupResult.status()) {
            case NOT_FOUND -> log.warn(
                    "OTT consume failed: token not found or already consumed. tokenHash={}",
                    tokenHash
            );
            case MALFORMED -> log.warn(
                    "OTT consume failed: malformed token payload in redis. tokenHash={}",
                    tokenHash
            );
            case SUCCESS -> throw new IllegalStateException("Success result must not be handled as failure");
        }

        increaseAttempts(tokenHash, ATTEMPT_TTL);
    }

    private void increaseAttempts(String subjectKey, Duration ttl) {
        Long attempts = redisTemplate.opsForValue().increment(RedisKeyFactory.ottAttempt(subjectKey));
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(RedisKeyFactory.ottAttempt(subjectKey), ttl);
        }
    }

    private int readAttempts(String subjectKey) {
        String count = redisTemplate.opsForValue().get(RedisKeyFactory.ottAttempt(subjectKey));
        if (count == null || count.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private enum LookupStatus {
        SUCCESS,
        NOT_FOUND,
        MALFORMED
    }

    private record TokenLookupResult(
            LookupStatus status,
            @Nullable String userId,
            @Nullable Instant expiresAt
    ) {
        private static TokenLookupResult success(String userId, Instant expiresAt) {
            return new TokenLookupResult(LookupStatus.SUCCESS, userId, expiresAt);
        }

        private static TokenLookupResult notFound() {
            return new TokenLookupResult(LookupStatus.NOT_FOUND, null, null);
        }

        private static TokenLookupResult malformed() {
            return new TokenLookupResult(LookupStatus.MALFORMED, null, null);
        }

        private boolean success() {
            return status == LookupStatus.SUCCESS;
        }
    }
}
