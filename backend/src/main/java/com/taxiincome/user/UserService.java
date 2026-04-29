package com.taxiincome.user;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.UserContext;
import com.taxiincome.security.DeviceTokenService;
import com.taxiincome.user.dto.InitUserRequest;
import com.taxiincome.user.dto.InitUserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserContext userContext;
    private final DeviceTokenService deviceTokenService;

    public UserService(UserRepository userRepository, UserContext userContext,
                        DeviceTokenService deviceTokenService) {
        this.userRepository = userRepository;
        this.userContext = userContext;
        this.deviceTokenService = deviceTokenService;
    }

    /**
     * Idempotent init: app cá nhân (single-user). Nếu đã có user trong DB thì trả về,
     * không tạo user thứ 2.
     */
    @Transactional
    public User initOrGetUser(InitUserRequest req) {
        String displayName = req.displayName() == null ? "" : req.displayName().trim();
        if (displayName.isBlank()) {
            throw ApiException.badRequest("INVALID_DISPLAY_NAME", "Tên không được để trống");
        }
        return userRepository.findFirstByOrderByCreatedAtAsc()
                .orElseGet(() -> {
                    User u = new User();
                    u.setId(UUID.randomUUID());
                    u.setDisplayName(displayName);
                    u.setNameLocked(true);
                    return userRepository.save(u);
                });
    }

    /**
     * Ensures user exists, then issues a new opaque access token (device session).
     */
    @Transactional
    public InitUserResponse initWithAccessToken(InitUserRequest req) {
        User user = initOrGetUser(req);
        String token = deviceTokenService.issueToken(user.getId());
        return InitUserResponse.of(user, token);
    }

    @Transactional(readOnly = true)
    public User currentUser() {
        UUID userId = userContext.requireUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND",
                        "Không tìm thấy user"));
    }
}
