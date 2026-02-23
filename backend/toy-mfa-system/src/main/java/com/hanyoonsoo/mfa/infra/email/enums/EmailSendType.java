package com.hanyoonsoo.mfa.infra.email.enums;

import com.hanyoonsoo.mfa.infra.redis.enums.AuthCacheEnum;

public enum EmailSendType {
    SIGN_UP(AuthCacheEnum.SIGN_UP),
    LOGIN(AuthCacheEnum.LOGIN),
    RESET_PASSWORD(AuthCacheEnum.RESET_PASSWORD),
    MFA_LOGIN(AuthCacheEnum.MFA_OTT);

    private final AuthCacheEnum cacheEnum;

    EmailSendType(AuthCacheEnum cacheEnum) {
        this.cacheEnum = cacheEnum;
    }

    public AuthCacheEnum getCacheEnum() {
        return cacheEnum;
    }
}
