package com.taxiincome.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Shared JSON body for servlet filters so shape matches {@link ApiError} / {@link GlobalExceptionHandler}.
 */
@Component
public class FilterJsonResponses {

    private final ObjectMapper objectMapper;

    public FilterJsonResponses(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response,
                      HttpStatus status,
                      String code,
                      String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ApiError(code, message));
    }

    public void unauthorized(HttpServletResponse response, String code, String message) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, code, message);
    }

    public void badRequest(HttpServletResponse response, String code, String message) throws IOException {
        write(response, HttpStatus.BAD_REQUEST, code, message);
    }
}
