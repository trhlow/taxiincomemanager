package com.taxiincome.common;

import com.taxiincome.security.AccessTokenHasher;
import com.taxiincome.security.DeviceTokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves the signed-in user from {@code Authorization: Bearer &lt;opaque token&gt;}.
 */
@Component
@Order(2)
public class BearerTokenFilter extends OncePerRequestFilter {

    private final ObjectProvider<UserContext> userContextProvider;
    private final DeviceTokenService deviceTokenService;
    private final FilterJsonResponses json;

    public BearerTokenFilter(ObjectProvider<UserContext> userContextProvider,
                             DeviceTokenService deviceTokenService,
                             FilterJsonResponses json) {
        this.userContextProvider = userContextProvider;
        this.deviceTokenService = deviceTokenService;
        this.json = json;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private static boolean isPublicInit(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/api/users/init".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (isPublicInit(request)) {
            chain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank()) {
            json.unauthorized(response, "MISSING_BEARER_TOKEN", "Thiếu Authorization: Bearer");
            return;
        }
        String trimmed = auth.trim();
        if (!trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            json.unauthorized(response, "INVALID_AUTHORIZATION", "Authorization phải dạng Bearer");
            return;
        }
        String raw = trimmed.substring(7).trim();
        if (raw.isEmpty()) {
            json.unauthorized(response, "MISSING_BEARER_TOKEN", "Token rỗng");
            return;
        }

        String hash = AccessTokenHasher.sha256Hex(raw);
        var userId = deviceTokenService.resolveUserAndTouch(hash);
        if (userId.isEmpty()) {
            json.unauthorized(response, "INVALID_ACCESS_TOKEN", "Token không hợp lệ hoặc đã thu hồi");
            return;
        }

        userContextProvider.getObject().setUserId(userId.get());
        chain.doFilter(request, response);
    }

}
