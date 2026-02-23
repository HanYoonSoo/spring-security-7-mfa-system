package com.hanyoonsoo.mfa.infra.redis.enums;

public enum AuthCacheEnum {
    SIGN_UP("SIGN_UP:", 1800000L),
    LOGIN("LOGIN:", 1800000L),
    RESET_PASSWORD("RESET_PASSWORD:", 1800000L),
    MFA_OTT("MFA_OTT:", 300000L);

    private final String prefix;
    private final long expirationTime;

    AuthCacheEnum(String prefix, long expirationTime) {
        this.prefix = prefix;
        this.expirationTime = expirationTime;
    }

    public String getPrefix() {
        return prefix;
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}
