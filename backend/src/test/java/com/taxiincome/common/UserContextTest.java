package com.taxiincome.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserContextTest {

    @Test
    void requireUserId_whenUnset_usesBearerOrientedMessage() {
        UserContext context = new UserContext();

        assertThatThrownBy(context::requireUserId)
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException a = (ApiException) ex;
                    assertThat(a.getCode()).isEqualTo("UNAUTHENTICATED");
                    assertThat(a.getMessage()).doesNotContain("X-User-Id");
                });
    }
}
