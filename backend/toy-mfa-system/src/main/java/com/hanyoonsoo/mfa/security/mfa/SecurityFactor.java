package com.hanyoonsoo.mfa.security.mfa;

import org.springframework.security.core.authority.FactorGrantedAuthority;

public final class SecurityFactor {
    private SecurityFactor() {
    }

    public static final String PASSWORD = FactorGrantedAuthority.PASSWORD_AUTHORITY;
    public static final String OTT = FactorGrantedAuthority.OTT_AUTHORITY;
}
