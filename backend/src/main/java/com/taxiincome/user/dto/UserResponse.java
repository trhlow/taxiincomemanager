package com.taxiincome.user.dto;

import com.taxiincome.user.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String displayName,
        boolean nameLocked,
        OffsetDateTime createdAt) {

    public static UserResponse of(User u) {
        return new UserResponse(u.getId(), u.getDisplayName(), u.isNameLocked(), u.getCreatedAt());
    }
}
