package com.taxiincome.user;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.UserContext;
import com.taxiincome.security.DeviceTokenService;
import com.taxiincome.user.dto.InitUserRequest;
import com.taxiincome.user.dto.InitUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserContext userContext;
    private final DeviceTokenService deviceTokenService;
    private final String setupSecret;

    public UserService(UserRepository userRepository, UserContext userContext,
                        DeviceTokenService deviceTokenService,
                        @Value("${app.setup.secret}") String setupSecret) {
        this.userRepository = userRepository;
        this.userContext = userContext;
        this.deviceTokenService = deviceTokenService;
        this.setupSecret = setupSecret;
    }

    /**
     * One-time init for the private single-user deployment.
     */
    @Transactional
    public User initUser(InitUserRequest req) {
        String displayName = req.displayName() == null ? "" : req.displayName().trim();
        if (displayName.isBlank()) {
            throw ApiException.badRequest("INVALID_DISPLAY_NAME", "Tên không được để trống");
        }
        if (!setupSecret.equals(req.setupSecret())) {
            throw ApiException.unauthorized("INVALID_SETUP_SECRET", "Setup secret không hợp lệ");
        }
        if (userRepository.existsAnyUser()) {
            throw alreadyInitialized();
        }

        try {
            User u = new User();
            u.setId(UUID.randomUUID());
            u.setDisplayName(displayName);
            u.setNameLocked(true);
            u.setSingletonKey("PRIMARY");
            return userRepository.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            throw alreadyInitialized();
        }
    }

    /**
     * Creates the first user, then issues the first opaque access token.
     */
    @Transactional
    public InitUserResponse initWithAccessToken(InitUserRequest req) {
        User user = initUser(req);
        String token = deviceTokenService.issueToken(user.getId());
        return InitUserResponse.of(user, token);
    }

    private static ApiException alreadyInitialized() {
        return ApiException.conflict(
                "USER_ALREADY_INITIALIZED",
                "Ứng dụng đã được khởi tạo. Không thể cấp token mới qua endpoint init.");
    }

    @Transactional(readOnly = true)
    public User currentUser() {
        UUID userId = userContext.requireUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND",
                        "Không tìm thấy user"));
    }
}
