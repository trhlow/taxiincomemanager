package com.taxiincome.security;

import com.taxiincome.common.ApiException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DeviceTokenService deviceTokenService;

    public AuthController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader("Authorization") String authorization) {
        String raw = rawBearerToken(authorization);
        deviceTokenService.revokeTokenHash(AccessTokenHasher.sha256Hex(raw));
        return Map.of("status", "OK");
    }

    private static String rawBearerToken(String authorization) {
        if (authorization == null) {
            throw ApiException.unauthorized("MISSING_BEARER_TOKEN", "Thiếu Authorization: Bearer");
        }
        String trimmed = authorization.trim();
        if (!trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw ApiException.unauthorized("INVALID_AUTHORIZATION", "Authorization phải dạng Bearer");
        }
        String raw = trimmed.substring(7).trim();
        if (raw.isEmpty()) {
            throw ApiException.unauthorized("MISSING_BEARER_TOKEN", "Token rỗng");
        }
        return raw;
    }
}
