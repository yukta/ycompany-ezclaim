package com.ycompany.claimservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RoleContextFilter extends OncePerRequestFilter {

    private static final String ROLE_HEADER = "X-ROLE";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String roleHeader = request.getHeader(ROLE_HEADER);

        if (roleHeader != null) {
            try {
                RoleContext.set(Role.valueOf(roleHeader));
            } catch (IllegalArgumentException ex) {
                response.sendError(
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid role in X-ROLE header"
                );
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            RoleContext.clear();
        }
    }
}
