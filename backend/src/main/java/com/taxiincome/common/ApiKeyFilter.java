package com.taxiincome.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Api-Key";

    private final String apiKey;

    private final FilterJsonResponses json;

    public ApiKeyFilter(@Value("${app.api-key}") String apiKey, FilterJsonResponses json) {
        this.apiKey = apiKey;
        this.json = json;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (apiKey == null || apiKey.isBlank() || !apiKey.equals(provided)) {
            json.unauthorized(response, "INVALID_API_KEY", "X-Api-Key không hợp lệ");
            return;
        }

        chain.doFilter(request, response);
    }
}
