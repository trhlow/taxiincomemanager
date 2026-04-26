package com.taxiincome.user;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.UserContext;
import com.taxiincome.user.dto.InitUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserContext userContext;

    public UserService(UserRepository userRepository, UserContext userContext) {
        this.userRepository = userRepository;
        this.userContext = userContext;
    }

    /**
     * Idempotent init: app cá nhân (single-user). Nếu đã có user trong DB thì trả về,
     * không tạo user thứ 2.
     */
    @Transactional
    public User initOrGetUser(InitUserRequest req) {
        return userRepository.findFirstByOrderByCreatedAtAsc()
                .orElseGet(() -> {
                    User u = new User();
                    u.setId(UUID.randomUUID());
                    u.setDisplayName(req.displayName().trim());
                    u.setNameLocked(true);
                    return userRepository.save(u);
                });
    }

    @Transactional(readOnly = true)
    public User currentUser() {
        UUID userId = userContext.requireUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND",
                        "Không tìm thấy user"));
    }
}
