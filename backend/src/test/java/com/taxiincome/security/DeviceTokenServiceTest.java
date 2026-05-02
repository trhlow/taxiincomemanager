package com.taxiincome.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-02T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneId.of("Asia/Ho_Chi_Minh"));

    @Test
    void issueToken_setsExpiryNinetyDaysFromNow() {
        DeviceTokenRepository repository = mock(DeviceTokenRepository.class);
        DeviceTokenService service = new DeviceTokenService(repository, CLOCK);

        String raw = service.issueToken(UUID.randomUUID());

        assertThat(raw).isNotBlank();
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(token ->
                token.getExpiresAt().isEqual(OffsetDateTime.ofInstant(NOW, CLOCK.getZone()).plusDays(90))
                        && token.getRevokedAt() == null));
    }

    @Test
    void resolveUserAndTouch_rejectsExpiredToken() {
        DeviceTokenRepository repository = mock(DeviceTokenRepository.class);
        DeviceTokenService service = new DeviceTokenService(repository, CLOCK);
        DeviceToken token = token();
        token.setExpiresAt(OffsetDateTime.ofInstant(NOW, CLOCK.getZone()).minusSeconds(1));

        when(repository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        assertThat(service.resolveUserAndTouch("hash")).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void resolveUserAndTouch_rejectsRevokedToken() {
        DeviceTokenRepository repository = mock(DeviceTokenRepository.class);
        DeviceTokenService service = new DeviceTokenService(repository, CLOCK);
        DeviceToken token = token();
        token.setRevokedAt(OffsetDateTime.ofInstant(NOW, CLOCK.getZone()).minusMinutes(1));

        when(repository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        assertThat(service.resolveUserAndTouch("hash")).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void revokeTokenHash_setsRevokedAt() {
        DeviceTokenRepository repository = mock(DeviceTokenRepository.class);
        DeviceTokenService service = new DeviceTokenService(repository, CLOCK);
        DeviceToken token = token();

        when(repository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        service.revokeTokenHash("hash");

        assertThat(token.getRevokedAt()).isEqualTo(OffsetDateTime.ofInstant(NOW, CLOCK.getZone()));
        verify(repository).save(token);
    }

    private static DeviceToken token() {
        DeviceToken token = new DeviceToken();
        token.setId(UUID.randomUUID());
        token.setUserId(UUID.randomUUID());
        token.setTokenHash("hash");
        token.setCreatedAt(OffsetDateTime.ofInstant(NOW, CLOCK.getZone()).minusDays(1));
        token.setExpiresAt(OffsetDateTime.ofInstant(NOW, CLOCK.getZone()).plusDays(1));
        return token;
    }
}
