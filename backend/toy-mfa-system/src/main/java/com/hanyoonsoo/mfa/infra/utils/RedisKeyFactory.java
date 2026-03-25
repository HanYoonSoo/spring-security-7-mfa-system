package com.hanyoonsoo.mfa.infra.utils;

import com.hanyoonsoo.mfa.common.Sha256HashUtils;
import com.hanyoonsoo.mfa.infra.redis.enums.AuthCacheEnum;

public final class RedisKeyFactory {

    private static final String DELIMITER = ":";

    private static final String USER_PREFIX = "user:";
    private static final String REFRESH_TOKEN_SUFFIX = "refresh-token";

    private static final String AUTH_PREFIX = "auth:";
    private static final String LOGOUT_ACCESS_TOKEN_PREFIX = AUTH_PREFIX + "logout:access-token:";

    private static final String OTT_TOKEN_PREFIX = "ott:token:";
    private static final String OTT_ATTEMPT_PREFIX = "ott:attempts:";

    private static final String MAGIC_LINK_LIMIT_PREFIX = "mfa:limit:";

    private RedisKeyFactory() {
    }

    public static String refreshToken(String userId) {
        return USER_PREFIX + userId + DELIMITER + REFRESH_TOKEN_SUFFIX;
    }

    public static String logoutAccessToken(String accessToken) {
        return LOGOUT_ACCESS_TOKEN_PREFIX + accessToken;
    }

    public static String authCode(AuthCacheEnum authCacheEnum, String loginId) {
        return AUTH_PREFIX + authCacheEnum.getPrefix() + DELIMITER + loginId;
    }

    public static String ottToken(String tokenHash) {
        return OTT_TOKEN_PREFIX + tokenHash;
    }

    public static String ottAttempt(String tokenHash) {
        return OTT_ATTEMPT_PREFIX + tokenHash;
    }

    public static String magicLinkLimit(String email) {
        return MAGIC_LINK_LIMIT_PREFIX + Sha256HashUtils.hash(email);
    }
}
