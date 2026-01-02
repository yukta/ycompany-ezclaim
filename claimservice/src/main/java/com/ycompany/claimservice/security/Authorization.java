package com.ycompany.claimservice.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Authorization {


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

    public static boolean hasRole(Role role) {
        Role current = RoleContext.get();
        return current != null && current == role;
    }

}
