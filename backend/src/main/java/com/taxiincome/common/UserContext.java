package com.taxiincome.common;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class UserContext {

    private UUID userId;

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID requireUserId() {
        if (userId == null) {
            throw ApiException.unauthorized("MISSING_USER",
                    "Thiếu header X-User-Id");
        }
        return userId;
    }
}
