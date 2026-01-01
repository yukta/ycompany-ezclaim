package com.ycompany.claimservice.security;

public final class RoleContext {

    private static final ThreadLocal<Role> CURRENT = new ThreadLocal<>();

    private RoleContext() {}

    public static void set(Role role) {
        CURRENT.set(role);
    }

    public static Role get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
