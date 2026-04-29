package com.taxiincome.user.dto;

import com.taxiincome.user.User;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response for {@code POST /api/users/init} — includes a one-time opaque access token.
 */
public record InitUserResponse(
        UUID id,
        String displayName,
        boolean nameLocked,
        OffsetDateTime createdAt,
        String accessToken
) {
    public static InitUserResponse of(User user, String accessToken) {
        return new InitUserResponse(
                user.getId(),
                user.getDisplayName(),
                user.isNameLocked(),
                user.getCreatedAt(),
                accessToken
        );
    }
}
