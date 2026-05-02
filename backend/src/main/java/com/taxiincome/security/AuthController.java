package com.taxiincome.security;

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
        String raw = authorization.trim().substring(7).trim();
        deviceTokenService.revokeTokenHash(AccessTokenHasher.sha256Hex(raw));
        return Map.of("status", "OK");
    }
}
