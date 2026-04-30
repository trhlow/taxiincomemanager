package com.taxiincome.user;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.UserContext;
import com.taxiincome.security.DeviceTokenService;
import com.taxiincome.user.dto.InitUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String SETUP_SECRET = "test-setup-secret";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserContext userContext;

    @Mock
    private DeviceTokenService deviceTokenService;

    @Test
    void initWithAccessToken_whenEmpty_createsPrimaryUserAndToken() {
        UserService service = new UserService(
                userRepository, userContext, deviceTokenService, SETUP_SECRET);
        UUID userId = UUID.randomUUID();

        when(userRepository.existsAnyUser()).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });
        when(deviceTokenService.issueToken(userId)).thenReturn("raw-token");

        var response = service.initWithAccessToken(
                new InitUserRequest(" Driver ", SETUP_SECRET));

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.displayName()).isEqualTo("Driver");
        assertThat(response.accessToken()).isEqualTo("raw-token");
    }

    @Test
    void initWithAccessToken_whenUserExists_returnsConflictAndDoesNotIssueToken() {
        UserService service = new UserService(
                userRepository, userContext, deviceTokenService, SETUP_SECRET);

        when(userRepository.existsAnyUser()).thenReturn(true);

        assertThatThrownBy(() -> service.initWithAccessToken(
                new InitUserRequest("Driver", SETUP_SECRET)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus().value()).isEqualTo(409);
                    assertThat(api.getCode()).isEqualTo("USER_ALREADY_INITIALIZED");
                });

        verify(deviceTokenService, never()).issueToken(any());
    }

    @Test
    void initWithAccessToken_whenConcurrentInsertWins_returnsConflictAndDoesNotIssueToken() {
        UserService service = new UserService(
                userRepository, userContext, deviceTokenService, SETUP_SECRET);

        when(userRepository.existsAnyUser()).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate singleton"));

        assertThatThrownBy(() -> service.initWithAccessToken(
                new InitUserRequest("Driver", SETUP_SECRET)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus().value()).isEqualTo(409);
                    assertThat(api.getCode()).isEqualTo("USER_ALREADY_INITIALIZED");
                });

        verify(deviceTokenService, never()).issueToken(any());
    }

    @Test
    void initWithAccessToken_withInvalidSetupSecret_returnsUnauthorized() {
        UserService service = new UserService(
                userRepository, userContext, deviceTokenService, SETUP_SECRET);

        assertThatThrownBy(() -> service.initWithAccessToken(
                new InitUserRequest("Driver", "wrong")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus().value()).isEqualTo(401);
                    assertThat(api.getCode()).isEqualTo("INVALID_SETUP_SECRET");
                });

        verify(deviceTokenService, never()).issueToken(any());
    }
}
