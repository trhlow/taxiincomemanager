package com.taxiincome.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class UserContextFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-User-Id";

    private final ObjectProvider<UserContext> userContextProvider;

    public UserContextFilter(ObjectProvider<UserContext> userContextProvider) {
        this.userContextProvider = userContextProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String raw = request.getHeader(HEADER);
        if (raw != null && !raw.isBlank()) {
            try {
                UUID parsed = UUID.fromString(raw.trim());
                userContextProvider.getObject().setUserId(parsed);
            } catch (IllegalArgumentException ignored) {
            }
        }
        chain.doFilter(request, response);
    }
}
