package com.taxiincome.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxiincome.security.AccessTokenHasher;
import com.taxiincome.security.DeviceTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level checks for {@link com.taxiincome.common.ApiKeyFilter} and
 * {@link com.taxiincome.common.BearerTokenFilter}. Runs only with {@code -Pintegration};
 * requires Docker for Testcontainers (skipped when Docker unavailable).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class SecurityFilterIT {

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Test
    void corsPreflight_allowsCurrentAuthHeadersOnly() throws Exception {
        MvcResult result = mockMvc.perform(options("/api/dashboard")
                        .header("Origin", "http://localhost:8080")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers",
                                "Authorization, Content-Type, X-Api-Key"))
                .andExpect(status().isOk())
                .andReturn();

        String allowHeaders = result.getResponse().getHeader("Access-Control-Allow-Headers");
        String exposeHeaders = result.getResponse().getHeader("Access-Control-Expose-Headers");

        assertThat(allowHeaders).contains("Authorization", "Content-Type", "X-Api-Key");
        assertThat(allowHeaders).doesNotContain("X-User-Id");
        assertThat(exposeHeaders).isNull();
    }

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
    void validApiKey_missingBearer_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MISSING_BEARER_TOKEN"));
    }

    @Test
    void validApiKey_invalidBearer_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .header("X-Api-Key", API_KEY)
                        .header("Authorization", "Bearer wrong-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_ACCESS_TOKEN"));
    }

    @Test
    void postInit_returnsAccessToken_thenDashboardOk() throws Exception {
        MvcResult init = mockMvc.perform(post("/api/users/init")
                        .header("X-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Integration","setupSecret":"test-setup-secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.id").isString())
                .andReturn();

        JsonNode root = objectMapper.readTree(
                init.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String token = root.path("accessToken").asText();
        String tokenHash = AccessTokenHasher.sha256Hex(token);

        assertThat(deviceTokenRepository.findByTokenHash(tokenHash)).isPresent();
        assertThat(deviceTokenRepository.findByTokenHash(tokenHash).get().getLastUsedAt()).isNull();

        mockMvc.perform(get("/api/dashboard")
                        .header("X-Api-Key", API_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTotalNet").exists());

        assertThat(deviceTokenRepository.findByTokenHash(tokenHash).get().getLastUsedAt()).isNotNull();
    }
}
