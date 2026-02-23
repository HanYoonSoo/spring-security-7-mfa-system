package com.hanyoonsoo.mfa.entity;

public enum UserRole {
    USER, ADMIN;

    public String toSpringRole() {
        return "ROLE_".concat(this.name());
    }
}
