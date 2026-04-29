package com.taxiincome.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOUCH_MIN_INTERVAL_MINUTES = 5;

    private final DeviceTokenRepository deviceTokenRepository;
    private final Clock clock;

    public DeviceTokenService(DeviceTokenRepository deviceTokenRepository, Clock clock) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.clock = clock;
    }

    private OffsetDateTime offsetNow() {
        return OffsetDateTime.ofInstant(clock.instant(), clock.getZone());
    }

    /**
     * Returns user id when token hash matches; refreshes {@code last_used_at} (throttled) in one transaction.
     */
    @Transactional
    public Optional<UUID> resolveUserAndTouch(String tokenHash) {
        Optional<DeviceToken> opt = deviceTokenRepository.findByTokenHash(tokenHash);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        DeviceToken token = opt.get();
        OffsetDateTime now = offsetNow();
        OffsetDateTime last = token.getLastUsedAt();
        if (last == null || last.isBefore(now.minusMinutes(TOUCH_MIN_INTERVAL_MINUTES))) {
            token.setLastUsedAt(now);
            deviceTokenRepository.save(token);
        }
        return Optional.of(token.getUserId());
    }

    /**
     * Persists hash of token; returns raw token once for the client (Authorization: Bearer ...).
     */
    @Transactional
    public String issueToken(UUID userId) {
        byte[] secret = new byte[32];
        RANDOM.nextBytes(secret);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        String hash = AccessTokenHasher.sha256Hex(raw);

        DeviceToken row = new DeviceToken();
        row.setId(UUID.randomUUID());
        row.setUserId(userId);
        row.setTokenHash(hash);
        row.setCreatedAt(offsetNow());
        deviceTokenRepository.save(row);
        return raw;
    }
}