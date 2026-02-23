package com.hanyoonsoo.mfa.infra.redis.service;

import com.hanyoonsoo.mfa.infra.email.enums.EmailSendType;
import com.hanyoonsoo.mfa.infra.redis.enums.AuthCacheEnum;
import com.hanyoonsoo.mfa.infra.redis.repository.RedisAuthCodeRepository;
import com.hanyoonsoo.mfa.infra.redis.repository.RedisRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthRedisService {

    public static final String AUTH_PREFIX = "AUTH:";

    private final RedisRefreshTokenRepository refreshTokenRedisRepository;
    private final RedisAuthCodeRepository authCodeRedisRepository;

    public void saveRefreshToken(String userId, String refreshToken, long expireMillis) {
        refreshTokenRedisRepository.saveRefreshToken(makeRefreshTokenKey(userId), refreshToken, expireMillis);
    }

    public void matchRefreshTokenOrThrow(String userId, String refreshToken) {
        refreshTokenRedisRepository.matchRefreshTokenOrThrow(makeRefreshTokenKey(userId), refreshToken);
    }

    public void deleteRefreshToken(String userId) {
        refreshTokenRedisRepository.deleteRefreshToken(makeRefreshTokenKey(userId));
    }

    public void saveAccessTokenForLogout(String accessToken, long expireMillis) {
        refreshTokenRedisRepository.saveAccessTokenForLogout(accessToken, expireMillis);
    }

    public boolean isLogoutAccessToken(String accessToken) {
        return refreshTokenRedisRepository.isLogoutAccessToken(accessToken);
    }

    public void saveAuthCode(String email, String authCode, EmailSendType emailSendType) {
        AuthCacheEnum authCacheEnum = emailSendType.getCacheEnum();
        authCodeRedisRepository.saveAuthCode(
                makeAuthCodeRedisKey(authCacheEnum, email),
                authCode,
                authCacheEnum.getExpirationTime()
        );
    }

    public String findAuthCode(String email, EmailSendType emailSendType) {
        AuthCacheEnum authCacheEnum = emailSendType.getCacheEnum();
        return authCodeRedisRepository.findAuthCode(makeAuthCodeRedisKey(authCacheEnum, email));
    }

    public void saveAuthCodeVerified(
            String email,
            String authCodeVerifiedStr,
            long expirationTime,
            EmailSendType emailSendType
    ) {
        AuthCacheEnum authCacheEnum = emailSendType.getCacheEnum();
        authCodeRedisRepository.saveAuthCodeVerified(
                makeAuthCodeRedisKey(authCacheEnum, email),
                authCodeVerifiedStr,
                expirationTime
        );
    }

    private String makeRefreshTokenKey(String userId) {
        return "user:" + userId + ":refreshToken";
    }

    public String makeAuthCodeRedisKey(AuthCacheEnum authCacheEnum, String loginId) {
        return AUTH_PREFIX + authCacheEnum.getPrefix() + loginId;
    }
}
