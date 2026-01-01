package com.ycompany.claimservice.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Authorization {

    private Authorization() {}

    public static void require(Role... allowedRoles) {

        Role current = RoleContext.get();

        if (current == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing role header"
            );
        }

        for (Role role : allowedRoles) {
            if (role == current) {
                return;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied for role: " + current
        );
    }
}
