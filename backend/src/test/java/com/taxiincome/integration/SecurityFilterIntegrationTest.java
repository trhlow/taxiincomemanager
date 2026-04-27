package com.taxiincome.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level checks for {@link com.taxiincome.common.ApiKeyFilter} and
 * {@link com.taxiincome.common.UserContextFilter}. Uses real PostgreSQL via Testcontainers
 * so Flyway migrations match production SQL (e.g. TIMESTAMPTZ).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class SecurityFilterIntegrationTest {

    private static final String API_KEY = "test-integration-api-key";

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void missingApiKey_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_API_KEY"));
    }

    @Test
    void wrongApiKey_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .header("X-Api-Key", "wrong-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_API_KEY"));
    }

    @Test
    void validApiKey_invalidUserIdFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .header("X-Api-Key", API_KEY)
                        .header("X-User-Id", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_USER_ID"));
    }

    @Test
    void validApiKey_missingUser_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MISSING_USER"));
    }
}
