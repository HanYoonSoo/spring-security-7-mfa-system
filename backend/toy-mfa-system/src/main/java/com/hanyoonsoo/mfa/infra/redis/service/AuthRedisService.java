package com.hanyoonsoo.mfa.infra.redis.service;

import com.hanyoonsoo.mfa.infra.email.enums.EmailSendType;
import com.hanyoonsoo.mfa.infra.redis.enums.AuthCacheEnum;
import com.hanyoonsoo.mfa.infra.redis.repository.RedisAuthCodeRepository;
import com.hanyoonsoo.mfa.infra.redis.repository.RedisRefreshTokenRepository;
import com.hanyoonsoo.mfa.infra.utils.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthRedisService {
    private final RedisRefreshTokenRepository refreshTokenRedisRepository;
    private final RedisAuthCodeRepository authCodeRedisRepository;

    public void saveRefreshToken(String userId, String refreshToken, long expireMillis) {
        refreshTokenRedisRepository.saveRefreshToken(RedisKeys.refreshToken(userId), refreshToken, expireMillis);
    }

    public void matchRefreshTokenOrThrow(String userId, String refreshToken) {
        refreshTokenRedisRepository.matchRefreshTokenOrThrow(RedisKeys.refreshToken(userId), refreshToken);
    }

    public void deleteRefreshToken(String userId) {
        refreshTokenRedisRepository.deleteRefreshToken(RedisKeys.refreshToken(userId));
    }

    public void saveAccessTokenForLogout(String accessToken, long expireMillis) {
        refreshTokenRedisRepository.saveAccessTokenForLogout(RedisKeys.logoutAccessToken(accessToken), expireMillis);
    }

    public boolean isLogoutAccessToken(String accessToken) {
        return refreshTokenRedisRepository.isLogoutAccessToken(RedisKeys.logoutAccessToken(accessToken));
    }

    public void saveAuthCode(String email, String authCode, EmailSendType emailSendType) {
        AuthCacheEnum authCacheEnum = emailSendType.getCacheEnum();
        authCodeRedisRepository.saveAuthCode(
                RedisKeys.authCode(authCacheEnum, email),
                authCode,
                authCacheEnum.getExpirationTime()
        );
    }

    public String findAuthCode(String email, EmailSendType emailSendType) {
        AuthCacheEnum authCacheEnum = emailSendType.getCacheEnum();
        return authCodeRedisRepository.findAuthCode(RedisKeys.authCode(authCacheEnum, email));
    }

    public void saveAuthCodeVerified(
            String email,
            String authCodeVerifiedStr,
            long expirationTime,
            EmailSendType emailSendType
    ) {
        AuthCacheEnum authCacheEnum = emailSendType.getCacheEnum();
        authCodeRedisRepository.saveAuthCodeVerified(
                RedisKeys.authCode(authCacheEnum, email),
                authCodeVerifiedStr,
                expirationTime
        );
    }
}
