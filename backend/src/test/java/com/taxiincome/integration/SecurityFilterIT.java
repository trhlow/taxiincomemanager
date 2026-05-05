package com.taxiincome.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxiincome.order.Order;
import com.taxiincome.order.OrderRepository;
import com.taxiincome.order.OrderSourceType;
import com.taxiincome.security.AccessTokenHasher;
import com.taxiincome.security.DeviceTokenService;
import com.taxiincome.security.DeviceTokenRepository;
import com.taxiincome.user.User;
import com.taxiincome.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level checks for {@link com.taxiincome.common.ApiKeyFilter} and
 * {@link com.taxiincome.common.BearerTokenFilter}. Runs only with {@code -Pintegration};
 * requires Docker for Testcontainers. The integration profile must fail when Docker is unavailable.
 */
@Testcontainers
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

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanDatabase() {
        deviceTokenRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

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
    void health_unauthenticated_returnsUp() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void postInit_returnsAccessToken_thenDashboardOk() throws Exception {
        MvcResult init = mockMvc.perform(post("/api/users/init")
                        .header("X-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Integration","setupSecret":"test-setup-secret"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.id").isString())
                .andReturn();

        JsonNode root = objectMapper.readTree(
                init.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String token = root.path("accessToken").asText();
        String tokenHash = AccessTokenHasher.sha256Hex(token);

        assertThat(deviceTokenRepository.findByTokenHash(tokenHash)).isPresent();
        assertThat(deviceTokenRepository.findByTokenHash(tokenHash).get().getLastUsedAt()).isNull();
        assertThat(deviceTokenRepository.findByTokenHash(tokenHash).get().getExpiresAt()).isNotNull();

        mockMvc.perform(get("/api/dashboard")
                        .header("X-Api-Key", API_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTotalNet").exists());

        assertThat(deviceTokenRepository.findByTokenHash(tokenHash).get().getLastUsedAt()).isNotNull();

        mockMvc.perform(post("/api/auth/logout")
                        .header("X-Api-Key", API_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

        assertThat(deviceTokenRepository.findByTokenHash(tokenHash).get().getRevokedAt()).isNotNull();

        mockMvc.perform(get("/api/dashboard")
                        .header("X-Api-Key", API_KEY)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_ACCESS_TOKEN"));
    }

    @Test
    void bearerToken_cannotReadAnotherUsersOrders() throws Exception {
        User alice = saveUser("Alice", "PRIMARY");
        User bob = saveUser("Bob", "LEGACY_BOB");
        String aliceToken = deviceTokenService.issueToken(alice.getId());
        String bobToken = deviceTokenService.issueToken(bob.getId());

        saveOrder(alice.getId(), 100_000L, 70_000L, LocalTime.of(9, 0));
        saveOrder(bob.getId(), 900_000L, 630_000L, LocalTime.of(10, 0));

        mockMvc.perform(get("/api/orders/by-date")
                        .header("X-Api-Key", API_KEY)
                        .header("Authorization", "Bearer " + aliceToken)
                        .param("date", "2026-05-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").value(1))
                .andExpect(jsonPath("$.totalOrderAmount").value(100_000))
                .andExpect(jsonPath("$.totalNet").value(70_000))
                .andExpect(jsonPath("$.orders[0].orderAmount").value(100_000));

        mockMvc.perform(get("/api/orders/by-date")
                        .header("X-Api-Key", API_KEY)
                        .header("Authorization", "Bearer " + bobToken)
                        .param("date", "2026-05-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").value(1))
                .andExpect(jsonPath("$.totalOrderAmount").value(900_000))
                .andExpect(jsonPath("$.totalNet").value(630_000))
                .andExpect(jsonPath("$.orders[0].orderAmount").value(900_000));
    }

    private User saveUser(String displayName, String singletonKey) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setDisplayName(displayName);
        user.setNameLocked(true);
        user.setSingletonKey(singletonKey);
        return userRepository.saveAndFlush(user);
    }

    private void saveOrder(UUID userId, long orderAmount, long netAmount, LocalTime time) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(userId);
        order.setOrderAmount(orderAmount);
        order.setFeeRate(new BigDecimal("0.300"));
        order.setFeeAmount(orderAmount * 30 / 100);
        order.setTipAmount(0);
        order.setTaxiCount((short) 1);
        order.setSubtotal(netAmount);
        order.setNetAmount(netAmount);
        order.setOrderDate(LocalDate.of(2026, 5, 2));
        order.setOrderTime(time);
        order.setSourceType(OrderSourceType.MANUAL);
        orderRepository.saveAndFlush(order);
    }
}
