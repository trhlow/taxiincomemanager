package com.taxiincome.user;

import com.taxiincome.user.dto.InitUserRequest;
import com.taxiincome.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/init")
    public UserResponse init(@Valid @RequestBody InitUserRequest req) {
        return UserResponse.of(userService.initOrGetUser(req));
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.of(userService.currentUser());
    }
}
